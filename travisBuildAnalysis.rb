#!/usr/bin/env ruby

require 'travis'
require 'csv'
require 'fileutils'
require 'find'

puts "Starting build and test analysis..."

indexProject=0
projectDirectory = []
pathProjects = []
parentsCommit = []
numberParents = 0

# Parametros:
# 1) o path do diretorio que apresenta os projetos a serem analisados
# Todo projeto deve apresentar apenas um arquivo ".travis.yml"
# 2) Path do diretorio que contem os commits a serem verificados
index = 0
ARGV.each do |parameter|
  projectDirectory[index] = parameter
  index += 1
end

# criacao dos diretorios onde os resultados serao salvos
FileUtils::mkdir_p 'TravisResults'
Dir.chdir "TravisResults"
pathResultByProject = Dir.pwd

# Mapeamento dos caminhos de cada um dos projetos
Find.find(projectDirectory[0]) do |path|
  pathProjects << path if path =~ /.*\.travis.yml$/
end
pathProjects.sort_by!{ |e| e.downcase }

# Mapeamento dos arquivos que reunem as informaçoes dos commits
csvFilesProjetcs = Dir[projectDirectory[1]+"**/*.csv"]
csvFilesProjetcs.sort_by!{ |e| e.downcase }

while (pathProjects.size > indexProject)

	Dir.chdir pathResultByProject
	projectName = ""
	Dir.chdir File.dirname(pathProjects[indexProject])
	config = %x(git remote show origin)
	config.each_line do |conf|
		if (conf.start_with?('  Fetch'))
            projectName = conf.partition('github.com/').last
            projectName = projectName.partition('.git').first
			break
		end
	end
	puts projectName

	buildProjeto = Travis::Repository.find(projectName)
	Dir.chdir pathResultByProject
	CSV.open(projectName.partition('/').last+"BUILDS.csv", "wb") do |csv|
	 	csv << ["Status", "Commit", "Build_ID"]
	end

	CSV.foreach(csvFilesProjetcs[indexProject], :headers => true) do |row|	
		buildProjeto.each_build do |build|
			Dir.chdir File.dirname(pathProjects[indexProject])
			#puts "ID: #{build.id} - Commit : #{row[0]}"
			if (build.commit.sha == row[0])
				filesConflict = %x(git diff --name-only #{build.commit.sha}^!)
				#verificando se o commit vem de um merge cenario
				if (filesConflict.size > 0)
					parentsCommit = []
					numberParents = 0
					commitType = %x(git cat-file -p #{build.commit.sha})
					commitType.each_line do |line|
						if(line.include?('author'))
							break
						end
						if(line.include?('parent'))
							parentsCommit[numberParents] = line.partition('parent ').last.gsub('\n','').gsub(' ','')
							numberParents += 1
						end	
					end
					if(numberParents > 1)
						# verificando o status de cada um dos pais do commit	
						parentsMergeOne = false
						parentsMergeTwo = false		
						buildProjeto.each_build do |mergeBuild|
							if (parentsMergeOne and parentsMergeTwo)
								break
							end
							if(parentsCommit[0].include?(mergeBuild.commit.sha) and mergeBuild.state=='passed')
								parentsMergeOne = true
							elsif (parentsCommit[1].include?(mergeBuild.commit.sha) and mergeBuild.state=='passed')
								parentsMergeTwo = true
							end
						end
						if (parentsMergeOne == true and parentsMergeTwo == true)
							Dir.chdir pathResultByProject
							CSV.open(projectName.partition('/').last+"BUILDS.csv", "a+") do |csv|
								csv << [build.state, build.commit.sha, build.id]
							end
							break
						end
					end
				end
			end
		end
	end
	indexProject += 1
end
puts "Finish :)"
exit
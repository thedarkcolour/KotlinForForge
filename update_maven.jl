#=
update_maven_local:
- Julia version: 1.8.4
- Author: thedarkcolour
- Date: 2023-01-02
=#
# Publish to maven local within this project
original_pwd = pwd()
# Compiles KFF and publishes it to local Maven within this folder
publish_special_maven() = run(`./gradlew -Dmaven.repo.local=$original_pwd publishAllMavens`)
# Executes the Gradle task when KotlinForForge project folder is in same folder as this website folder
cd(publish_special_maven, "../KotlinForForge")

for (root, dir, files) in walkdir("thedarkcolour")
  for file_name in files
    file = joinpath(root, file_name)

    if (endswith(file_name, "local.xml"))
      cp(file, replace(file, "-local.xml" => ".xml"), force=true)
    elseif (!contains(file_name, "local.xml"))
      run(`git add $file`)
    end
  end
end

# Regenerate hashes
include("generate_checksums.jl")
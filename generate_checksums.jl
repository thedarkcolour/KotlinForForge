Pkg.add("MD5")

using SHA
using MD5

write_hash(path, hash_function) = write(open(path * "." * String(Symbol(hash_function)), "w"), bytes2hex(open(hash_function, path)))

for (root, dir, files) in walkdir("thedarkcolour")
  for file_name in files
    if (endswith(file_name, ".jar") || endswith(file_name, ".pom") || endswith(file_name, ".xml"))
      path = joinpath(root, file_name)
      write_hash(path, md5)
      write_hash(path, sha1)
    end
  end
end
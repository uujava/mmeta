require 'bundler/setup'
require 'ant'
require 'rake/testtask'

task :default => :test
task :jar => 'dist/mmeta.jar'

task :clean do
  ant.delete :quiet => true, :dir => 'build'
  ant.delete :quiet => true, :dir => 'dist'
end

task :bootstrap => ['dist/mmeta.jar'] do
  runjava 'dist/mmeta.jar', '--auto_memo', 'boot/parser.mmeta', 'boot/parser.mirah'
  runjava 'dist/mmeta.jar', 'boot/compiler.mmeta', 'boot/compiler.mirah'
end

file 'dist/mmeta-runtime.jar' => Dir.glob('mmeta/*.{java,mirah}') + ['build/runtime', 'dist', 'javalib/mirahc-prev.jar'] do
  ENV['BS_CHECK_CLASSES'] = 'true'
  mirahc('mmeta/ast.mirah', :dest => 'build/runtime')
  ant.javac :srcDir=>'mmeta', :destDir=>'build/runtime', :debug=>true
  ant.jar :destfile=>'dist/mmeta-runtime.jar', :basedir=>'build/runtime'
end

file 'build/boot/mmeta/MMetaParser.class' => ['boot/parser.mirah', 'build/boot/mmeta', 'dist/mmeta-runtime.jar', 'javalib/mirahc-prev.jar' ] do
  cp 'boot/parser.mirah', 'build/boot/mmeta/'
  mirahc('mmeta/parser.mirah',
         :dir => 'build/boot',
         :dest => 'build/boot',
         :options => ['--classpath', 'dist/mmeta-runtime.jar'])
end

file 'build/boot/mmeta/MMetaCompiler.class' => ['boot/compiler.mirah', 'build/boot/mmeta', 'dist/mmeta-runtime.jar' , 'javalib/mirahc-prev.jar'] do
  cp 'boot/compiler.mirah', 'build/boot/mmeta/'
  mirahc('mmeta/compiler.mirah',
         :dir  => 'build/boot',
         :dest => 'build/boot',
         :options => [
           '--classpath', "build/boot#{File::PATH_SEPARATOR}dist/mmeta-runtime.jar#{File::PATH_SEPARATOR}javalib/hapax-2.3.5-autoindent.jar"
  ])
end

file 'dist/mmeta.jar' => ['dist/mmeta-runtime.jar',
                          'build/boot/mmeta/MMetaParser.class',
                          'build/boot/mmeta/MMetaCompiler.class'] + Dir['boot/templates/*.xtm'] do
  cp_r 'boot/templates', 'build/boot/mmeta/'
  ant.jar :destfile => 'dist/mmeta.jar' do
    fileset :dir => "build/boot", :includes => "mmeta/*.class"
    fileset :dir => "build/boot", :includes => "mmeta/templates/*.xtm"
    zipfileset :includes => "**/*.class",
               :src      => "javalib/hapax-2.3.5-autoindent.jar"
    zipfileset :includes => "mmeta/*.class",
               :src      => 'dist/mmeta-runtime.jar'
    manifest do
      attribute :name => "Main-Class", :value => "mmeta.MMetaCompiler"
    end
  end
end

namespace :test do
  task :compile => ['dist/mmeta.jar', 'build/test', 'build/test/MirahLexer.class']
  file 'build/test/MirahLexer.class' => ['test/MirahLexer.java', 'test/Tokens.java', 'javalib/mirahc-prev.jar'] do

    cp "test/MirahLexer.java", "build/test/"
    cp "test/Tokens.java",     "build/test/"

    ant.javac :srcDir    => 'build/test',
              :classpath => 'dist/mmeta-runtime.jar',
              :debug     => true,
              :destDir => 'build'

    mirahc 'test',
           :dir     => 'build',
           :dest    => 'build',
           :options => [
             '--classpath', "dist/mmeta-runtime.jar#{File::PATH_SEPARATOR}build"
           ]
  end
  task :calc => :'test:compile' do
    runjava('test.Calculator2',
            '4 * 3 - 2',
            :outputproperty => 'test.output2',
            :classpath      => "dist/mmeta-runtime.jar#{File::PATH_SEPARATOR}build",
            :failonerror    => false)
    if ant.properties['test.output2'].to_s.strip == '10'
      puts "Mirah Calculator passed"
    else
      puts "Expected calculator result 10, got #{ant.properties['test.output2']}"
      exit(1)
    end
  end
  Rake::TestTask.new :parser do |t|
    t.libs << 'build/test'
    t.test_files = FileList['test/test_parser.rb']
  end
  task :parser => ['test:compile']
end

def test_grammar(name, *options)
  task('build/test/MirahLexer.class').enhance ["build/test/#{name}.mirah"]
  file "build/test/#{name}.mirah" => ["test/#{name}.mmeta", 'dist/mmeta.jar', 'build/test'] do
    cp "test/#{name}.mmeta", "build/test/"
    args = ['dist/mmeta.jar', *options]
    args.concat ["build/test/#{name}.mmeta", "build/test/#{name}.mirah"]
    runjava  *args
  end
end

test_grammar('MirahCalculator', '--auto_memo', '--recursion')
#test_grammar('Mirah')
test_grammar('TestParser')
test_grammar('Java')
test_grammar('Left', '--auto_memo', '--recursion')

task :test => [:'test:calc',
               :'test:parser',
               ]

directory 'dist'
directory 'build/test'
directory 'build/mmeta'
directory 'build/runtime'
directory 'build/boot/mmeta'

file_create 'javalib/mirahc-prev.jar' do
  require 'open-uri'

  url = ENV['MIRAH_PREV_PATH'] || 'https://search.maven.org/remotecontent?filepath=org/mirah/mirah/0.1.4/mirah-0.1.4.jar'

  puts "Downloading mirahc-prev.jar from #{url}"

  open(url, 'rb') do |src|
    open('javalib/mirahc-prev.jar', 'wb') do |dest|
      dest.write(src.read)
    end
  end
end

def runjava(jar, *args)
  options = {:failonerror => true, :fork => true}
  if jar =~ /\.jar$/
    options[:jar] = jar
  else
    options[:classname] = jar
  end
  options.merge!(args.pop) if args[-1].kind_of?(Hash)
  puts "java #{options} " + args.join(' ')
  ant.java options do
    args.each do |value|
      arg :value => value
    end
  end
end

def mirahc(source, options)
  puts "calling mirahc #{source} #{options}"
  args = []
  if options[:dest]
    args += ['-d', options[:dest]]
  end
  if options[:options]
    args += options[:options]
  end
  if options[:dir]
    args += [options[:dir]+'/'+source]
  else
    args += [source]
  end

  runjava 'javalib/mirahc-prev.jar', *args
end
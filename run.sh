cd "$(dirname "$0")"
mkdir -p out
javac -cp "lib/*" -d out src/jdm/Main.java src/jdm/model/*.java src/jdm/repository/*.java src/jdm/service/*.java src/jdm/alert/*.java
java -cp "out:lib/*" jdm.Main
read
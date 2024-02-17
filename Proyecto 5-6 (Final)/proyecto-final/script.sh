gnome-terminal -- java -jar ./target/proyecto-final-1.0-SNAPSHOT-jar-with-dependencies.jar

cd target/classes

gnome-terminal -- bash -c "mvn exec:java -Dexec.mainClass='com.proyectofinal.ServidorLibros' -Dexec.args='8081 0'"
gnome-terminal -- bash -c "mvn exec:java -Dexec.mainClass='com.proyectofinal.ServidorLibros' -Dexec.args='8082 1'"
gnome-terminal -- bash -c "mvn exec:java -Dexec.mainClass='com.proyectofinal.ServidorLibros' -Dexec.args='8083 2'"


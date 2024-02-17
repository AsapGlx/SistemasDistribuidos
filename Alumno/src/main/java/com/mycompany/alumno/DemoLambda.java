package com.mycompany.alumno;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DemoLambda {
  
    public List<Alumno> listaAlumnos = new ArrayList<>();
        
    public DemoLambda() {
        listaAlumnos = new ArrayList<>();
        listaAlumnos.add(new Alumno(1, "1717213183", "Javier Ignacio", "Molina Cano", "Java 8", 7, 28));
        listaAlumnos.add(new Alumno(2, "1717456218", "Lillian Eugenia", "Gómez Álvarez", "Java 8", 10, 33));
        listaAlumnos.add(new Alumno(3, "1717328901", "Sixto Naranjoe", "Marín", "Java 8", 8.6, 15));
        listaAlumnos.add(new Alumno(4, "1717567128", "Gerardo Emilio", "Duque Gutiérrez", "Java 8", 10, 13));
        listaAlumnos.add(new Alumno(5, "1717902145", "Jhony Alberto", "Sáenz Hurtado", "Java 8", 9.5, 15));
        listaAlumnos.add(new Alumno(6, "1717678456", "Germán Antonio", "Lotero Upegui", "Java 8", 8, 34));
        listaAlumnos.add(new Alumno(7, "1102156732", "Oscar Darío", "Murillo González", "Java 8", 8, 32));
        listaAlumnos.add(new Alumno(8, "1103421907", "Augusto Osorno", "Palacio Martínez", "PHP", 9.5, 17));
        listaAlumnos.add(new Alumno(9, "1717297015", "César Oswaldo", "Alzate Agudelo", "Java 8", 8, 26));
        listaAlumnos.add(new Alumno(10, "1717912056", "Gloria Amparo", "González Castaño", "PHP", 10, 28));
        listaAlumnos.add(new Alumno(11, "1717912058", "Jorge León", "Ruiz Ruiz", "Python", 8, 22));
        listaAlumnos.add(new Alumno(12, "1717912985", "John Jairo", "Duque García", "Java Script", 9.4, 32));
        listaAlumnos.add(new Alumno(13, "1717913851", "Julio Cesar", "González Castaño", "C Sharp", 10, 22));
        listaAlumnos.add(new Alumno(14, "1717986531", "Gloria Amparo", "Rodas Monsalve", "Ruby", 7, 18));
        listaAlumnos.add(new Alumno(15, "1717975232", "Gabriel Jaime", "Jiménez Gómez", "Java Script", 10, 18));
    }
     public static void main(String[] args) {
        DemoLambda demo = new DemoLambda();
        demo.Ejemplo1();
        demo.Ejemplo2();
        demo.Ejemplo3();
        demo.Ejemplo4();
        demo.Ejemplo5();
        demo.Ejemplo6();
        demo.Ejemplo7();
        demo.Ejemplo8();
        demo.Ejemplo9();
        demo.Ejemplo10();
        demo.Ejemplo11();
        demo.Ejemplo12();
        demo.Ejemplo13();
    }
     
   /**
 * Ejemplo1: Imprime la lista completa de alumnos dos veces.
 */
public void Ejemplo1(){
    System.out.println("*** Lista de Alumnos ***");
    listaAlumnos.stream().forEach(a->System.out.println(a));
    listaAlumnos.stream().filter(a -> true).forEach(a -> System.out.println(a));
}

/**
 * Ejemplo2: Imprime los alumnos cuyos apellidos empiezan con la letra 'L' o 'G'.
 */
public void Ejemplo2(){
    System.out.println("\n*** Alumnos cuyo nombre empiezan con el caracter L u G ***");
    listaAlumnos.stream()
                .filter(c -> c.getApellidos().charAt(0) == 'L' || c.getApellidos().charAt(0) == 'G')
                .forEach(c -> System.out.println(c));
}

/**
 * Ejemplo3: Imprime el número total de alumnos en la lista.
 */
public void Ejemplo3(){
    System.out.println("\n**** Número de Alumnos ***");
    System.out.println(listaAlumnos.stream().count());
}

/**
 * Ejemplo4: Imprime los alumnos que tienen una nota mayor a 9 y están en el curso "PHP".
 */
public void Ejemplo4(){
    System.out.println("\n**** Alumnos con nota mayor a 9 y que sean del curso PHP ***");
    listaAlumnos.stream()
                .filter(a -> a.getNota() > 9 && a.getNombreCurso().equals("PHP"))
                .forEach(p -> System.out.println(p));
}

/**
 * Ejemplo5: Imprime los primeros 2 alumnos de la lista.
 */
public void Ejemplo5(){
    System.out.println("\n**** Imprimir los 2 primeros Alumnos de la lista ***");
    listaAlumnos.stream().limit(2).forEach(a -> System.out.println(a));
}

/**
 * Ejemplo6: Imprime el alumno con la menor edad.
 */
public void Ejemplo6(){
    System.out.println("\n**** Imprimir el alumno con menor edad ***");
    System.out.println(listaAlumnos.stream().min((a1, a2) -> a1.getEdad() - a2.getEdad()));
}

/**
 * Ejemplo7: Imprime el alumno con la mayor edad.
 */
public void Ejemplo7(){
    System.out.println("\n**** Imprimir el alumno con mayor edad ***");
    System.out.println(listaAlumnos.stream().max((a1, a2) -> a1.getEdad() - a2.getEdad()));
}

/**
 * Ejemplo8: Imprime el primer alumno de la lista.
 */
public void Ejemplo8(){
    System.out.println("\n**** Encontrar el primer Alumno***");
    System.out.println(listaAlumnos.stream().findFirst());
}

/**
 * Ejemplo9: Imprime los alumnos cuyos nombres de cursos terminan con la letra 't'.
 */
public void Ejemplo9(){
    System.out.println("\n**** Alumnos en los que los nombres de los cursos (lenguajes) terminan en t ***");
    listaAlumnos.stream().filter(a -> a.getNombreCurso().endsWith("t")).forEach(System.out::println);
}

/**
 * Ejemplo10: Imprime los alumnos que tienen un curso cuyo nombre contiene la letra 'A'.
 */
public void Ejemplo10(){
    System.out.println("\n**** Alumnos que tienen un curso en el que el nombre contienen la A***");
    listaAlumnos.stream().filter(a -> a.getNombreCurso().contains("a")).forEach(System.out::println);
}

/**
 * Ejemplo11: Imprime los alumnos cuyo nombre tiene más de 10 caracteres.
 */
public void Ejemplo11(){
    System.out.println("\n**** Alumnos en que su tamaño de su nombre es mayor a 10 caracteres***");
    listaAlumnos.stream().filter(a -> a.getNombres().length() > 10).forEach(System.out::println);
}

/**
 * Ejemplo12: Imprime los alumnos cuyo nombre del curso empieza con 'P' y tiene una longitud menor o igual a 6 caracteres.
 */
public void Ejemplo12(){
    System.out.println("\n**** Combinación de predicados ***");
    Predicate<Alumno> empiezaConJ = a -> a.getNombreCurso().startsWith("P");
    Predicate<Alumno> longitud = a -> a.getNombreCurso().length() <= 6;
    listaAlumnos.stream().filter(empiezaConJ.and(longitud)).forEach(System.out::println);
}

/**
 * Ejemplo13: Crea una nueva lista con los alumnos cuyo nombre del curso contiene la letra 'a' e imprime esta nueva lista.
 */
public void Ejemplo13(){
    System.out.println("\n**** Alumnos cuyo nombre del curso contiene la letra 'a' ***");
    List<Alumno> nuevaLista= listaAlumnos.stream().filter(a -> a.getNombreCurso().contains("a")).collect(Collectors.toList());
    nuevaLista.forEach(System.out::println);
}
      
}

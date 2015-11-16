package nl.vu.ai.aso;

import ec.Evolve;

/**
 * Hello world!
 *
 */
public class App {

    public static void main( String[] args ) {
        System.out.println( "Hello World!" );

        Evolve.main(new String[]{"-file", App.class.getClassLoader().getResource("ecj.params").getPath()});
    }

}

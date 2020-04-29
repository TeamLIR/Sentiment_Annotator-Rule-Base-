import java.util.Scanner;

public class mainAnnotator {

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);  // Create a Scanner object

        System.out.println("Enter String");
        String input = sc.nextLine();  // Read user input

        System.out.println("Enter Petitioner members");
        String petitioner = sc.nextLine();  // Read user input

        System.out.println("Enter Defendant members");
        String defendant = sc.nextLine();  // Read user input
        StanfordAnnotator.stanfordAnnotator(input,petitioner,defendant);
        Annotator.Annotator(input,petitioner,defendant);
    }
}

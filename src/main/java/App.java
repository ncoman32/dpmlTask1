public class App {

    public static void main(String[] args) {

        final MagicSquare magicSquare = new MagicSquare(3);
        int[][][] result = magicSquare.getResult();

        for (int resultNo = 0; resultNo < magicSquare.getNumberOfValidConfigurations(); resultNo++) {
            System.out.println("Magic square found!");
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    System.out.print(result[resultNo][i][j] + " ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }
}

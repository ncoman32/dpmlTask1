public class App {

  public static void main(String[] args) {

    //final MagicSquare magicSquare = new MagicSquare(3);
    //magicSquare.simpleBackTrack(0, 0);
    //magicSquare.printSolutions();

    final MapColoring mapColoring = new MapColoring(5, 3);
    //mapColoring.backTrackWithAC3(0);
    mapColoring.backTrackWithForwardChecking(0);
    mapColoring.printSolutions();

  }
}

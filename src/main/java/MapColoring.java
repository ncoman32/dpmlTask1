import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class MapColoring {

  /**
   * The number of colors used to solve the problem. Must be at most n.
   */
  int numberOfColors;
  /**
   * The number of nodes of the graph.
   */
  private int n;
  /**
   * The graph configuration.
   */
  private int[][] graph;
  /**
   * The colors of the nodes. color[i] = the color of node i.
   * <br>
   * -1 value if no color is assigned for a node.
   * <br>
   * Colors are represented as digits: 0, 1, 2 ...
   */
  private int[] colors;

  /**
   * Holds the solutions found by the algorithm.
   */
  private int[][] solutions;

  /**
   * Keeps track of the number of solutions found
   */
  private int numberOfSolutionsFound;

  private Map<Integer, String> colorsMap;

  /**
   * Keeps track of the domain of each variable(vertex).
   */
  private Map<Integer, List<Integer>> domainsMap;

  private Queue<Constraint> constraints;

  public MapColoring(int n, int numberOfColors) {
    this.n = n;
    if (numberOfColors > n) {
      throw new IllegalArgumentException("The number of colors must be at "
          + "most the number of nodes in the map coloring problem");
    }
    this.numberOfColors = numberOfColors;
    this.colors = new int[n];
    for (int i = 0; i < n; i++) {
      colors[i] = -1;
    }
    graph = new int[n][n];
    constraints = new LinkedList<>();
    this.domainsMap = new HashMap<>();
    generateRandomGraph();
    this.numberOfSolutionsFound = -1;
    this.solutions = new int[100][n];
    initColorsMap();
    printGraph();
  }

  public void simpleBacktrack(int nodeToColor) {
    // if all nodes have been assigned a color, return true
    if (n == nodeToColor) {
      copySolution();
      return;
    }

    for (int colorToTry = 0; colorToTry < this.numberOfColors; colorToTry++) {
      if (validateColorForNode(colorToTry, nodeToColor)) {

        colors[nodeToColor] = colorToTry;

        simpleBacktrack(nodeToColor + 1);

        colors[nodeToColor] = -1;
      }
    }
  }

  private boolean validateColorForNode(int colorToTry, int nodeToColor) {
    for (int i = 0; i < n; i++) {
      if (graph[nodeToColor][i] == 1 && colors[i] == colorToTry) {
        return false;
      }
    }
    return true;
  }

  public void backTrackWithAC3(int nodeToStart) {
    System.out.println("BEFORE AC3");
    domainsMap.put(0, Arrays.asList(1));
    printConstraintsGraphInfo();
    runAC3();

    System.out.println("AFTER AC3");
    printConstraintsGraphInfo();

    if (isAnyDomainEmpty()) {
      System.out.println("There is at least one domain empty. The problem has no solution!");
      return;
    }

    runBacktrack(nodeToStart);

  }

  private void runBacktrack(int nodeToProcess) {
    if (nodeToProcess == n) {
      copySolution();
      return;
    }

    List<Integer> vertexDomain = domainsMap.get(nodeToProcess);

    for (Integer colorToTry : vertexDomain) {
      if (validateColorForNode(colorToTry, nodeToProcess)) {

        colors[nodeToProcess] = colorToTry;

        runBacktrack(nodeToProcess + 1);

        colors[nodeToProcess] = -1;
      }
    }
  }

  private boolean isAnyDomainEmpty() {
    return domainsMap.values().stream().anyMatch(List::isEmpty);
  }

  private void runAC3() {
    while (!constraints.isEmpty()) {
      Constraint constraintToProcess = constraints.poll();
      checkConstraintUpdateAgendaAndDomains(constraintToProcess);
    }
  }

  private void checkConstraintUpdateAgendaAndDomains(Constraint constraintToProcess) {
    List<Integer> vertex1Domain = domainsMap.get(constraintToProcess.getV1());
    List<Integer> vertex2Domain = domainsMap.get(constraintToProcess.getV2());
    List<Integer> updatedVertex1Domain = new ArrayList<>(vertex1Domain);

    for (Integer colorV1 : vertex1Domain) {
      if (!isValueOk(colorV1, vertex2Domain)) {
        updatedVertex1Domain.remove(colorV1);
      }
    }

    //update domain
    domainsMap.put(constraintToProcess.getV1(), updatedVertex1Domain);

    //update the agenda
    List<Constraint> constraintsToAdd =
        getConstraintsToAnalyzeAfterVertexDomainModification(constraintToProcess.getV1());

    addConstraintsIfNotPresent(constraintsToAdd);
  }


  private boolean isValueOk(Integer colorV1, List<Integer> vertex2Domain) {
    boolean ok = false;
    for (Integer colorV2 : vertex2Domain) {
      if (!colorV1.equals(colorV2)) {
        ok = true;
        break;
      }
    }
    return ok;
  }

  private List<Constraint> getConstraintsToAnalyzeAfterVertexDomainModification(
      int modifiedVertex) {
    return constraints.stream().filter(constraint -> constraint.getV2() == modifiedVertex)
        .collect(Collectors.toList());
  }

  private void addConstraintsIfNotPresent(List<Constraint> constraintsToAdd) {
    for (Constraint constraint : constraintsToAdd) {
      if (!isConstraintPresent(constraint)) {
        System.out.println(
            "ADDING CONSTRAINT BETWEEN " + constraint.getV1() + " AND" + constraint.getV2()
                + " TO THE AGENDA");
        constraints.add(constraint);
      }
    }
  }

  private boolean isConstraintPresent(Constraint constraintToCheck) {
    return constraints.stream()
        .anyMatch(constraint -> constraint.getV1() == constraintToCheck.getV1()
            && constraint.getV2() == constraintToCheck.getV2());
  }

  public void backTrackWithForwardChecking(int nodeToColor) {
    printConstraintsGraphInfo();
    if (n == nodeToColor || allDomainsHaveOneColor()) {
      copySolutionForForwardChecking();
      return;
    }

    List<Integer> nodeDomain = domainsMap.get(nodeToColor);

    for (Integer color : nodeDomain) {

      // Update the current node and neighbors domains according to the assignment
      Map<Integer, List<Integer>> backUpDomains = new HashMap<>();
      for (int j = 0; j < n; j++) {
        if (graph[nodeToColor][j] == 1) {
          backUpDomains.put(j, domainsMap.get(j));
          List<Integer> neighborNewDomainList = domainsMap.get(j).stream()
              .filter(Objects::nonNull)
              .filter(nodeColor -> !nodeColor.equals(color))
              .collect(Collectors.toList());
          if (neighborNewDomainList.isEmpty()) {
            return;
          }
          domainsMap.put(j, neighborNewDomainList);
        }
      }
      domainsMap.put(nodeToColor, Arrays.asList(color));

      backTrackWithForwardChecking(nodeToColor + 1);

      // Rollback domain changes if no solution found
      backUpDomains.forEach((key, value) -> domainsMap.put(key, value));
    }

  }

  private boolean allDomainsHaveOneColor() {
    boolean anyNullValueInDomain = domainsMap.values().stream()
        .anyMatch(list -> list.size() == 1 && isNull(list.get(0)));

    if (anyNullValueInDomain) {
      return false;
    }
    return domainsMap.values().stream().map(List::size)
        .allMatch(size -> size == 1);
  }


  public void printSolutions() {
    for (int solutionNo = 0; solutionNo < numberOfSolutionsFound; solutionNo++) {
      System.out.println("Solution " + solutionNo);
      for (int i = 0; i < n; i++) {
        System.out.print(" Node[" + i + "] = " + colorsMap.get(solutions[solutionNo][i]));
      }
      System.out.println();
    }
  }

  private void generateRandomGraph() {
    Random rnd = new Random();
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (i != j) {
          graph[i][j] = rnd.nextBoolean() ? 1 : 0;
          graph[j][i] = graph[i][j];
        }
      }
    }

    initializeDomains();

    // detect linked nodes and build the initial agenda
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (graph[i][j] == 1) {
          constraints.add(new Constraint(i, j, ConstraintType.DIFFERENT));
        }
      }
    }
  }

  private void initializeDomains() {
    List<Integer> colorsList =
        IntStream.range(0, numberOfColors).boxed().collect(Collectors.toList());
    IntStream.range(0, n).forEach(vertex -> domainsMap.put(vertex, colorsList));
  }

  private void printGraph() {
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        System.out.print(graph[i][j] + " ");
      }
      System.out.println();
    }
  }

  private void printConstraintsGraphInfo() {
    System.out.println();
    System.out.println("Constraint graph has edges: ");
    constraints.stream().forEach(constraint -> System.out.println(
        constraint.getV1() + " " + constraint.getV2() + " having constraint " + constraint.getType()
            .name()));
    domainsMap.entrySet().forEach(
        entry -> System.out
            .println("Domain of vertex " + entry.getKey() + ": is " + entry.getValue())
    );
  }

  private void copySolution() {
    this.numberOfSolutionsFound++;
    for (int i = 0; i < n; i++) {
      this.solutions[numberOfSolutionsFound][i] = colors[i];
    }
  }

  private void copySolutionForForwardChecking() {
    this.numberOfSolutionsFound++;
    domainsMap.forEach((key, value) -> solutions[numberOfSolutionsFound][key] = (int) value
        .toArray()[0]);
  }

  private void initColorsMap() {
    this.colorsMap = new HashMap<>();
    colorsMap.put(0, "RED");
    colorsMap.put(1, "BLUE");
    colorsMap.put(2, "GREEN");
    colorsMap.put(3, "YELLOW");
    colorsMap.put(4, "ORANGE");
    colorsMap.put(5, "PURPLE");
    colorsMap.put(6, "BROWN");
    colorsMap.put(7, "GRAY");
    colorsMap.put(8, "TURQUOISE");
    colorsMap.put(9, "MAGENTA");
    colorsMap.put(10, "CYAN");
  }

  enum ConstraintType {
    DIFFERENT,
    GREATER,
    LESS
  }

  class Constraint {

    /**
     * The first vertex involved of the constraint
     */
    private int v1;

    /**
     * The second vertex involved in the constraint
     */
    private int v2;

    private ConstraintType type;

    public Constraint(int v1, int v2, ConstraintType type) {
      this.v1 = v1;
      this.v2 = v2;
      this.type = type;
    }

    public int getV1() {
      return v1;
    }

    public int getV2() {
      return v2;
    }

    public ConstraintType getType() {
      return type;
    }
  }
}


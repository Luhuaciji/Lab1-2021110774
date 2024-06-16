import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.security.SecureRandom;
import org.apache.commons.io.FilenameUtils;

/**
 * 类GraphProcesser.
 */
public class GraphProcessor {
  Map<String, Map<String, Integer>> graph = new HashMap<>();
  private static final SecureRandom SR = new SecureRandom();

  /**
   * main.
   *
   * @param args args
   */
  public static void main(String[] args) {
    GraphProcessor processor = new GraphProcessor();
    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

    System.out.println("请输入文本文件路径：");
    String filePath = scanner.nextLine();
    processor.readFileAndGenerateGraph(filePath);

    while (true) {
      System.out.println("请选择操作：");
      System.out.println("1. 展示有向图");
      System.out.println("2. 查询桥接词");
      System.out.println("3. 根据桥接词生成新文本");
      System.out.println("4. 计算两个单词之间的最短路径");
      System.out.println("5. 随机游走");
      System.out.println("6. 退出");

      int choice = scanner.nextInt();
      scanner.nextLine(); // consume newline

      switch (choice) {
        case 1:
          processor.showDirectedGraph();
          break;
        case 2:
          System.out.println("请输入两个单词：");
          String word1 = scanner.next();
          String word2 = scanner.next();
          System.out.println(processor.queryBridgeWords(word1, word2));
          break;
        case 3:
          System.out.println("请输入新文本：");
          String inputText = scanner.nextLine();
          System.out.println(processor.generateNewText(inputText));
          break;
        case 4:
          System.out.println("请输入两个单词：");
          word1 = scanner.next();
          word2 = scanner.next();
          System.out.println(processor.calcShortestPath(word1, word2));
          break;
        case 5:
          System.out.println(processor.randomWalk());
          break;
        case 6:
          return;
        default:
          System.out.println("无效的选择");
      }
    }
  }

  /**
   * 读取文件，提取单词转化成有向图.
   *
   * @param filePath 读取的文件路径
   */
  public void readFileAndGenerateGraph(String filePath) {
    String safePath = FilenameUtils.getName(filePath);
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(safePath),
        StandardCharsets.UTF_8))) {
      String line;
      String previousWord = null;
      while ((line = br.readLine()) != null) {
        //先转换成小写，再将非小写字母和空格的字符转换成空格，最后根据空格将语句分割成字符串数组
        String[] words = line.toLowerCase().replaceAll("[^a-z\\s]", " ").split("\\s+");
        for (String word : words) {
          if (word.isEmpty()) {
            continue;
          }
          //更新边权值
          if (previousWord != null) {
            graph.putIfAbsent(previousWord, new HashMap<>());
            graph.get(previousWord).put(word,
                graph.get(previousWord).getOrDefault(word, 0) + 1);
          }
          previousWord = word;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 功能1. 展示有向图.
   */
  public void showDirectedGraph() {
    //GraphViz中的实现
    GraphViz gv = new GraphViz();
    gv.addln(gv.start_graph());
    //结点和边权值添加
    for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
      String from = entry.getKey();
      for (Map.Entry<String, Integer> toEntry : entry.getValue().entrySet()) {
        String to = toEntry.getKey();
        int weight = toEntry.getValue();
        gv.addEdgeWithLabel(from, to, String.valueOf(weight));
      }
    }
    gv.addln(gv.end_graph());
    //绘图
    //String type = "png";
    String type = FilenameUtils.getName("graph.png");
    File out = new File(type);
    byte[] img = gv.getGraph(gv.getDotSource(), type);
    if (img != null) {
      gv.writeGraphToFile(img, out);
      System.out.println("Graph generated successfully: " + out.getAbsolutePath());
    } else {
      System.err.println("Error generating graph!");
    }
  }

  /**
   * 功能2. 查询桥接词.
   *
   * @param word1 单词1
   * @param word2 单词2
   * @return 返回一个字符串，包含全部桥接词
   */
  public String queryBridgeWords(String word1, String word2) {
    if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
      return "No " + word1 + " or " + word2 + " in the graph!";
    }
    //哈希表实现的集合，用于存储找到的桥接词
    Set<String> bridgeWords = new HashSet<>();
    //对word1指向的每一个单词，判断该单词是否指向word2，若是则为桥接词，存入集合
    for (String word : graph.get(word1).keySet()) {
      if (graph.containsKey(word) && graph.get(word).containsKey(word2)) {
        bridgeWords.add(word);
      }
    }
    //如果集合为空，没有桥接词
    if (bridgeWords.isEmpty()) {
      return "No bridge words from " + word1 + " to " + word2 + "!";
    } else {
      return "The bridge words from " + word1 + " to " + word2 + " are: "
          + String.join(", ", bridgeWords) + ".";
    }
  }

  /**
   * 功能3. 根据桥接词生成新文本.
   *
   * @param inputText 输入文本
   * @return 返回添加完桥接词之后的文本
   */
  public String generateNewText(String inputText) {
    String[] words = inputText.toLowerCase().replaceAll("[^a-z\\s]", " ").split("\\s+");
    StringBuilder newText = new StringBuilder();
    GraphViz gv = new GraphViz();
    gv.addln(gv.start_graph());

    for (int i = 0; i < words.length - 1; i++) {
      newText.append(words[i]).append(" ");
      String bridgeWord = getBridgeWord(words[i], words[i + 1]);
      if (bridgeWord != null) {
        newText.append(bridgeWord).append(" ");
        int weight = calculateWeight(words[i], words[i + 1], inputText);
        gv.addln(words[i] + " [style=filled, fillcolor=yellow];");
        gv.addln(words[i + 1] + " [style=filled, fillcolor=yellow];");
        gv.addln(bridgeWord + " [style=filled, fillcolor=blue];");

        gv.addln(words[i] + " -> " + bridgeWord + " [color=blue, label=\"" + weight
            + "\"];");
        gv.addln(bridgeWord + " -> " + words[i + 1] + " [color=blue, label=\"" + weight
            + "\"];");
      } else {
        int weight = calculateWeight(words[i], words[i + 1], inputText);
        gv.addln(words[i]);
        gv.addln(words[i] + " -> " + words[i + 1] + " [label=\"" + weight + "\"];");
      }
    }
    gv.addln(gv.end_graph());
    //绘图
    String type = FilenameUtils.getName("graph.png");
    File out = new File(type);
    byte[] img = gv.getGraph(gv.getDotSource(), type);
    if (img != null) {
      gv.writeGraphToFile(img, out);
      System.out.println("Graph generated successfully: " + out.getAbsolutePath());
    } else {
      System.err.println("Error generating graph!");
    }
    newText.append(words[words.length - 1]);
    return newText.toString();
  }

  private int calculateWeight(String word1, String word2, String inputText) {
    // 计算边的权重：word1和word2在inputText中相邻出现的次数
    String[] words = inputText.split("\\s+");
    int count = 0;
    for (int i = 0; i < words.length - 1; i++) {
      if (words[i].equals(word1) && words[i + 1].equals(word2)) {
        count++;
      }
    }
    return count;
  }

  //获取桥接词
  private String getBridgeWord(String word1, String word2) {
    List<String> bridgeWords = new ArrayList<>();
    //getOrDefault方法返回word1为key查找到的的哈希表中所有key的集合
    for (String word : graph.getOrDefault(word1, new HashMap<>()).keySet()) {
      //存在 word1->word->word2，则将word存入集合
      if (graph.getOrDefault(word, new HashMap<>()).containsKey(word2)) {
        bridgeWords.add(word);
      }
    }
    if (bridgeWords.isEmpty()) {
      return null;
    }
    //若有多个桥接词，则随机选择一个
    return bridgeWords.get(SR.nextInt(bridgeWords.size()));
  }

  /**
   * 功能4. 计算两个单词之间的最短路径
   * 使用Dijkstra算法计算单源最短路径
   *
   * @param word1 起始单词
   * @param word2 终点单词
   * @return 返回最短路径
   */
  public String calcShortestPath(String word1, String word2) {
    if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
      return "No " + word1 + " or " + word2 + " in the graph!";
    }
    Map<String, Integer> distances = new HashMap<>();
    Map<String, String> path = new HashMap<>();
    PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));
    //开始时，将所有节点的距离设为最大
    for (String node : graph.keySet()) {
      distances.put(node, Integer.MAX_VALUE);
    }
    //到自己的距离设为0，并存入队列
    distances.put(word1, 0);
    queue.add(word1);
    GraphViz gv = new GraphViz();
    gv.addln(gv.start_graph());
    //队列非空
    while (!queue.isEmpty()) {
      String current = queue.poll();
      if (current.equals(word2)) {
        break;
      }
      //计算当允许以current为中间节点时到其他节点的距离是否更短
      for (String neighbor : graph.get(current).keySet()) {
        int newDist = distances.get(current) + graph.get(current).get(neighbor);
        //若更短，则更新distance和path，并将当前节点加入队列
        if (newDist < distances.get(neighbor)) {
          distances.put(neighbor, newDist);
          path.put(neighbor, current);
          queue.add(neighbor);
        }
      }
    }
    //距离未更新，则不可达
    if (distances.get(word2) == Integer.MAX_VALUE) {
      return "No path from " + word1 + " to " + word2 + "!";
    }

    List<String> shortestpath = new LinkedList<>();
    //借助path中反向寻找路径，添加到shortestpath中
    for (String at = word2; at != null; at = path.get(at)) {
      shortestpath.add(at);
    }
    //反转shortestpath，得到最短路径
    Collections.reverse(shortestpath);

    gv.addln(word1 + " [style=filled, fillcolor=yellow];");
    gv.addln(word2 + " [style=filled, fillcolor=yellow];");
    String preWord = word1;
    for (String node : shortestpath) {
      if (node.equals(word1)) {
        continue;
      }
      if (!node.equals(word2)) {
        gv.addln(node + " [style=filled, fillcolor=blue];");
      }
      int weight = graph.get(preWord).get(node);
      gv.addln(preWord + " -> " + node + " [color=blue, label=\"" + weight + "\"];");
      preWord = node;
    }

    for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
      String from = entry.getKey();
      for (Map.Entry<String, Integer> toEntry : entry.getValue().entrySet()) {
        String to = toEntry.getKey();
        if (shortestpath.contains(from) && shortestpath.contains(to)
            && shortestNextNode(shortestpath, from, to)) {
          continue;
        }
        int weight = toEntry.getValue();
        if (!shortestpath.contains(from)) {
          gv.addln(from);
        }
        gv.addln(from + " -> " + to + " [label=\"" + weight + "\"];");
        if (!shortestpath.contains(to)) {
          gv.addln(to);
        }
      }
    }

    gv.addln(gv.end_graph());
    //绘图
    String type = FilenameUtils.getName("graph.png");
    File out = new File(type);
    byte[] img = gv.getGraph(gv.getDotSource(), type);
    if (img != null) {
      gv.writeGraphToFile(img, out);
      System.out.println("Graph generated successfully: " + out.getAbsolutePath());
    } else {
      System.err.println("Error generating graph!");
    }

    //String.join方法将节点使用"->"连接
    return "Shortest path: " + String.join(" -> ", shortestpath)
        + " (Length: " + distances.get(word2) + ")";
  }

  /**
   * 计算最短距离使用的方法.
   *
   * @param shortestpath 最短路径字符串
   * @param word1        单词1
   * @param word2        单词2
   * @return 返回bool类型
   */
  public boolean shortestNextNode(List<String> shortestpath, String word1, String word2) {
    for (int i = 0; i < shortestpath.size() - 1; i++) {
      if (word1.equals(shortestpath.get(i)) && word2.equals(shortestpath.get(i + 1))) {
        return true;
      }
    }
    return false;
  }

  /**
   * 功能5. 随机游走.
   *
   * @return 返回随机游走字符串
   */
  public String randomWalk() {
    List<String> nodes = new ArrayList<>(graph.keySet());
    if (nodes.isEmpty()) {
      return "";
    }
    //随机选择起始节点
    String current = nodes.get(SR.nextInt(nodes.size()));
    System.out.print(current + " ");
    //记录访问过的边
    Set<String> visitedEdges = new HashSet<>();
    StringBuilder walk = new StringBuilder(current);
    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

    while (true) {
      /*
      // 等待用户输入
      String inPut = scanner.nextLine();
      if (!inPut.isEmpty()) {
        break;
      }
       */

      //将当前节点的另据节点哈希表提取出来
      Map<String, Integer> neighbors = graph.get(current);
      if (neighbors == null || neighbors.isEmpty()) {
        break;
      }
      //随机选择一个邻居节点作为next
      List<String> edges = new ArrayList<>(neighbors.keySet());
      String next = edges.get(SR.nextInt(edges.size()));
      //构建边，若已访问
      final String edge = current + " -> " + next;
      System.out.print(next + " ");
      walk.append(" ").append(next);
      current = next;
      //出现重复的边，停止随机游走
      if (visitedEdges.contains(edge)) {
        break;
      }
      visitedEdges.add(edge);
    }
    System.out.println();

    // 将结果写入文件
    try (BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(
            new FileOutputStream("random_walk.txt"), StandardCharsets.UTF_8))) {
      writer.write(walk.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return walk.toString();
  }

  public void gitLabedit() {
    System.out.println("对Lab1进行若干修改，对其进行本地仓库提交操作");
  }
}

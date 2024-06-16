import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GraphProcessorWTest {

  private GraphProcessor graphProcessor;

  @BeforeEach
  void setUp() {
    graphProcessor = new GraphProcessor();
  }

  /**
   * 空图随机游走.
   */
  @Test
  void testRandomWalk_EmptyGraph() {
    assertEquals("", graphProcessor.randomWalk());
  }

  @Test
  void testRandomWalk_SingleNode() {
    graphProcessor.graph.put("A", new HashMap<>());
    assertEquals("A", graphProcessor.randomWalk());
  }

  @Test
  void testRandomWalk_MultipleNodes_NoEdges() {
    graphProcessor.graph.put("A", new HashMap<>());
    graphProcessor.graph.put("B", new HashMap<>());
    String result = graphProcessor.randomWalk();
    assertTrue(result.equals("A") || result.equals("B"));
  }

  @Test
  void testRandomWalk_MultipleNodes_NoRepeatedEdges() {
    graphProcessor.graph.put("A", Map.of("B", 1));
    graphProcessor.graph.put("B", Map.of("C", 1));
    graphProcessor.graph.put("C", new HashMap<>());
    String result = graphProcessor.randomWalk();
    assertTrue(result.equals("A B C") || result.equals("B C") || result.equals("C"));
  }

  @Test
  void testRandomWalk_MultipleNodes_WithRepeatedEdges() {
    graphProcessor.graph.put("A", Map.of("B", 1));
    graphProcessor.graph.put("B", Map.of("C", 1, "A", 1));
    graphProcessor.graph.put("C", Map.of("A", 1));
    String result = graphProcessor.randomWalk();
    assertTrue(result.equals("A B C A B") || result.equals("A B A B") || result.equals("B C A B C")
        || result.equals("B A B A") || result.equals("B A B C A B") || result.equals("B C A B A B") ||
        result.equals("C A B C A") || result.equals("C A B A B"));
  }

  /*
  @Test
  void testRandomWalk_UserInputInterrupt() {
    // 这里我们需要模拟用户输入，这在普通的单元测试中比较困难，
    // 但我们可以通过传递自定义的输入流来实现。
    // 例如，使用 System.setIn 来传递一个包含输入数据的 ByteArrayInputStream。
    ByteArrayInputStream in = new ByteArrayInputStream("stop\n".getBytes(StandardCharsets.UTF_8));
    System.setIn(in);

    graphProcessor.graph.put("A", Map.of("B", 1));
    graphProcessor.graph.put("B", Map.of("C", 1));
    graphProcessor.graph.put("C", new HashMap<>());

    String result = graphProcessor.randomWalk();
    assertTrue(result.contains("A"));
  }
   */
}

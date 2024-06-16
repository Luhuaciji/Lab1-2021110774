import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

public class GraphProcessorTest {
  private GraphProcessor graphProcessor;

  @BeforeEach
  public void setUp() {
    graphProcessor = new GraphProcessor();
  }

  @Test
  public void testQueryBridgeWords_BothWordsExistAndHaveBridgeWords() {
    graphProcessor.graph.put("the", Map.of("quick", 1));
    graphProcessor.graph.put("quick", Map.of("dog", 1));
    graphProcessor.graph.put("dog", Map.of());

    String result = graphProcessor.queryBridgeWords("the", "dog");
    assertEquals("The bridge words from the to dog are: quick.", result);
  }

  @Test
  public void testQueryBridgeWords_BothWordsExistButNoBridgeWords() {
    graphProcessor.graph.put("the", Map.of("quick", 1, "cat", 1));
    graphProcessor.graph.put("quick", Map.of("dog", 1));
    graphProcessor.graph.put("dog", Map.of());
    graphProcessor.graph.put("cat", Map.of());

    String result = graphProcessor.queryBridgeWords("the", "cat");
    assertEquals("No bridge words from the to cat!", result);
  }

  @Test
  public void testQueryBridgeWords_Word1DoesNotExist() {
    graphProcessor.graph.put("the", Map.of("quick", 1));
    graphProcessor.graph.put("quick", Map.of("dog", 1));
    graphProcessor.graph.put("dog", Map.of());

    String result = graphProcessor.queryBridgeWords("fox", "dog");
    assertEquals("No fox or dog in the graph!", result);
  }

  @Test
  public void testQueryBridgeWords_Word2DoesNotExist() {
    graphProcessor.graph.put("the", Map.of("quick", 1));
    graphProcessor.graph.put("quick", Map.of("fox", 1));
    graphProcessor.graph.put("fox", Map.of());

    String result = graphProcessor.queryBridgeWords("dog", "fox");
    assertEquals("No dog or fox in the graph!", result);
  }

  @Test
  public void testQueryBridgeWords_BothWordsDoNotExist() {
    graphProcessor.graph.put("the", Map.of("quick", 1));
    graphProcessor.graph.put("quick", Map.of("dog", 1));
    graphProcessor.graph.put("dog", Map.of());

    String result = graphProcessor.queryBridgeWords("cat", "fox");
    assertEquals("No cat or fox in the graph!", result);
  }

  @Test
  public void testQueryBridgeWords_EmptyGraph() {
    String result = graphProcessor.queryBridgeWords("the", "dog");
    assertEquals("No the or dog in the graph!", result);
  }

  @Test
  public void testQueryBridgeWords_BothWordsExistAndHaveMoreThan2BridgeWords() {
    graphProcessor.graph.put("the", Map.of("quick", 1, "fat", 1));
    graphProcessor.graph.put("quick", Map.of("dog", 1));
    graphProcessor.graph.put("fat", Map.of("dog", 1));
    graphProcessor.graph.put("dog", Map.of());
    String result = graphProcessor.queryBridgeWords("the", "dog");
    assertEquals("The bridge words from the to dog are: quick, fat.", result);
  }
}
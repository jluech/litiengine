package de.gurkenlabs.litiengine.entities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.GameWorld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntityTests {

  @Test
  public void testSetX() {
    // arrange
    TestEntity entitySpy = new TestEntity();
    assertEquals(0, entitySpy.getX());

    // act
    entitySpy.setX(5);

    // assert
    assertEquals(5, entitySpy.getX());
  }

  @Test
  public void testSetY() {
    // arrange
    TestEntity entitySpy = new TestEntity();
    assertEquals(0, entitySpy.getY());

    // act
    entitySpy.setY(5);

    // assert
    assertEquals(5, entitySpy.getY());
  }

  @Test
  public void testHasTag() {
    // arrange
    String tag = "test tag";
    TestEntity entitySpy = new TestEntity();
    entitySpy.addTag(tag);

    // act, assert
    assertTrue(entitySpy.hasTag(tag));
  }

  @Test
  public void testAddTagNotContained() {
    // arrange
    String tag = "test tag";
    TestEntity entitySpy = new TestEntity();
    assertFalse(entitySpy.hasTag(tag));

    // act
    entitySpy.addTag(tag);

    // assert
    assertTrue(entitySpy.hasTag(tag));
  }

  @Test
  public void testAddTagContained() {
    // arrange
    String tag = "test tag";
    List<String> tagListSpy = spy(Collections.singletonList(tag));
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.getTags()).thenReturn(tagListSpy);

    // act
    entitySpy.addTag(tag);

    // assert
    verify(tagListSpy, times(1)).contains(tag);
    verify(tagListSpy, times(0)).add(tag);
  }

  @Test
  public void testAddTagEnvironmentNull() {
    // arrange
    String tag = "test tag";
    TestEntity entitySpy = spy(new TestEntity());
    Environment envMock = mock(Environment.class);
    entitySpy.loaded(envMock);
    when(entitySpy.getEnvironment()).thenReturn(null);

    // act
    entitySpy.addTag(tag);

    // assert
    verify(envMock, times(0)).getEntitiesByTag();
  }

  @Test
  public void testAddTagEnvironmentValid() {
    // arrange
    String tag = "test tag";
    TestEntity entitySpy = spy(new TestEntity());
    Environment envMock = mock(Environment.class);
    when(entitySpy.getEnvironment()).thenReturn(envMock);

    // act
    entitySpy.addTag(tag);

    // assert
    verify(envMock, times(1)).getEntitiesByTag();
  }

  @Test
  public void testRemoveTagGameEnvironmentNull() {
    // arrange
    String tag = "test tag";
    List<String> tagListSpy = spy(new ArrayList<>());
    tagListSpy.add(tag);
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.getTags()).thenReturn(tagListSpy);

    MockedStatic<Game> gameMockedStatic = mockStatic(Game.class);
    GameWorld gameWorldMock = mock(GameWorld.class);
    when(gameWorldMock.environment()).thenReturn(null);
    gameMockedStatic.when(Game::world).thenReturn(gameWorldMock);

    // act
    entitySpy.removeTag(tag);

    // assert
    verify(tagListSpy, times(1)).remove(tag);
    verify(entitySpy, times(0)).getEnvironment();

    // cleanup
    gameMockedStatic.close();
  }

  @Test
  public void testRemoveTagEmpty() {
    // arrange
    String tag = "test tag";
    List<String> tagListSpy = spy(new ArrayList<>());
    tagListSpy.add(tag);
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.getTags()).thenReturn(tagListSpy);

    Environment environmentMock = mock(Environment.class);
    Map<String, Collection<IEntity>> entitiesSpy = new HashMap<>();
    List<IEntity> entitiesContent = new ArrayList<>();
    entitiesContent.add(entitySpy);
    entitiesSpy.put(tag, entitiesContent);
    when(environmentMock.getEntitiesByTag()).thenReturn(entitiesSpy);
    when(entitySpy.getEnvironment()).thenReturn(environmentMock);

    MockedStatic<Game> gameMockedStatic = mockStatic(Game.class);
    GameWorld gameWorldMock = mock(GameWorld.class);
    when(gameWorldMock.environment()).thenReturn(mock(Environment.class));
    gameMockedStatic.when(Game::world).thenReturn(gameWorldMock);

    assertEquals(1, entitiesSpy.size());

    // act
    entitySpy.removeTag(tag);

    // assert
    verify(tagListSpy, times(1)).remove(tag);
    assertEquals(0, entitiesSpy.size());

    // cleanup
    gameMockedStatic.close();
  }

  @Test
  public void testToStringNameNull() {
    // arrange
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.getMapId()).thenReturn(5);
    when(entitySpy.getName()).thenReturn(null);

    // act
    String result = entitySpy.toString();

    // assert
    verify(entitySpy, times(1)).getName(); // second in if is dropped when first is false
    assertEquals("#5: TestEntity", result);
  }

  @Test
  public void testToStringNameEmpty() {
    // arrange
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.getMapId()).thenReturn(5);
    when(entitySpy.getName()).thenReturn("");

    // act
    String result = entitySpy.toString();

    // assert
    verify(entitySpy, times(2)).getName();
    assertEquals("#5: TestEntity", result);
  }

  @Test
  public void testToStringNameValid() {
    // arrange
    TestEntity entitySpy = spy(new TestEntity());
    when(entitySpy.getMapId()).thenReturn(5);
    when(entitySpy.getName()).thenReturn("test name");

    // act
    String result = entitySpy.toString();

    // assert
    verify(entitySpy, times(3)).getName();
    assertEquals("#5: test name", result);
  }

  @Test
  public void testLoaded() {
    // arrange
    TestEntity entity = new TestEntity();

    EntityListener listenerMock = mock(EntityListener.class);
    entity.addListener(listenerMock);

    Environment envMock = mock(Environment.class);

    assertNull(entity.getEnvironment());
    assertFalse(entity.isLoaded());

    // act
    entity.loaded(envMock);

    // act, assert
    assertEquals(envMock, entity.getEnvironment());
    assertTrue(entity.isLoaded());
    verify(listenerMock, times(1)).loaded(entity, envMock);
  }

  @Test
  public void testRemoved() {
    // arrange
    TestEntity entity = new TestEntity();

    Environment envMock = mock(Environment.class);
    entity.loaded(envMock);

    EntityListener listenerMock = mock(EntityListener.class);
    entity.addListener(listenerMock);

    assertEquals(envMock, entity.getEnvironment());
    assertTrue(entity.isLoaded());

    // act
    entity.removed(envMock);

    // act, assert
    assertNull(entity.getEnvironment());
    assertFalse(entity.isLoaded());
    verify(listenerMock, times(1)).removed(entity, envMock);
  }

  @Test
  void testEntityAction() {
    TestEntity entity = new TestEntity();

    assertTrue(entity.actions().exists("doSomething"));
    assertFalse(entity.actions().exists("imNotAnAction"));

    assertEquals(2, entity.actions().getActions().size());

    EntityAction action = entity.actions().get("doSomething");

    assertEquals("doSomething", action.getName());
    assertEquals("does something", action.getDescription());

    assertNotNull(action);

    action.perform();

    assertTrue(entity.didSomething);

    entity.actions().unregister(action);

    assertEquals(1, entity.actions().getActions().size());
    assertFalse(entity.actions().exists("doSomething"));

    entity.actions().register(action);

    assertEquals(2, entity.actions().getActions().size());
    assertTrue(entity.actions().exists("doSomething"));
  }

  @Test
  void testNamedAction() {
    TestEntity entity = new TestEntity();

    assertTrue(entity.actions().exists("myName"));

    EntityAction action = entity.actions().get("myName");

    assertNotNull(action);

    action.perform();

    assertTrue(entity.didNamedAction);
  }

  @Test
  void testCustomAction() {
    TestEntity entity = new TestEntity();
    entity.register("customAction", () -> entity.customActionPerformed = true);

    assertTrue(entity.actions().exists("customAction"));

    entity.perform("customAction");
    assertTrue(entity.customActionPerformed);

    entity.actions().unregister("customAction");

    assertFalse(entity.actions().exists("customAction"));

    assertDoesNotThrow(() -> entity.perform("I don't exist!"));
  }

  @ParameterizedTest
  @MethodSource("getDefaultTags")
  void testDefaultTags(String tag) {
    TestEntity entity = new TestEntity();
    assertTrue(entity.hasTag(tag));
  }

  private static Stream<Arguments> getDefaultTags() {
    return Stream.of(
        Arguments.of("some tag"),
        Arguments.of("another tag")
    );
  }

  @Tag("some tag")
  @Tag("another tag")
  private static class TestEntity extends Entity {
    private boolean didSomething;
    private boolean didNamedAction;
    private boolean customActionPerformed;

    @Action(description = "does something")
    public void doSomething() {
      didSomething = true;
    }

    @Action(name = "myName")
    public void namedAction() {
      didNamedAction = true;
    }

    @SuppressWarnings("unused")
    public void imNotAnAction() {
    }

    @Action
    public void imNotParameterless(int something) {
    }

    @Action
    private void privateAction() {
    }
  }
}

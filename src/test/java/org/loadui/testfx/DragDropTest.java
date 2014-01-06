package org.loadui.testfx;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBoxBuilder;

import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import static org.loadui.testfx.Assertions.verifyThat;
import static org.hamcrest.Matchers.*;
import static org.loadui.testfx.controls.ListViews.containsRow;
import static org.loadui.testfx.controls.ListViews.numberOfRowsIn;

public class DragDropTest extends GuiTest {

    private final ListView<String> list1 = new ListView<>();
    private final ListView<String> list2 = new ListView<>();

    /**
     * A typical event handler to start a drag-drop operation.
     */
    private final EventHandler<MouseEvent> onDragDetected = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {
            Object draggedItem = ((ListView<?>) event.getSource()).getSelectionModel().getSelectedItem();
            ClipboardContent content = new ClipboardContent();
            content.putString(draggedItem.toString());

            Dragboard dragboard = ((Node) event.getSource()).startDragAndDrop(TransferMode.MOVE);
            dragboard.setContent(content);
            event.consume();
        }
    };

    /**
     * A typical event handler to delete the source item from the source list after the drag-drop operation completed.
     */
    private final EventHandler<DragEvent> onDragDone = new EventHandler<DragEvent>() {
        public void handle(DragEvent event) {
            String item = event.getDragboard().getString();
            ((ListView<?>) event.getSource()).getItems().remove(item);
        }
    };

    /**
     * A typical event handler to accept drag events.
     */
    private final EventHandler<DragEvent> onDragOver = new EventHandler<DragEvent>() {
        public void handle(DragEvent event) {
            event.acceptTransferModes(TransferMode.MOVE);
            System.out.println("HERE");
        }
    };
    
    /**
     * A typical event handler to handle drop events.
     */
    private final EventHandler<DragEvent> onDragDropped = new EventHandler<DragEvent>() {
        public void handle(DragEvent event) {
            
            System.out.println("THERE");
            event.acceptTransferModes(TransferMode.MOVE);
            String item = event.getDragboard().getString();
            if (item != null) {
                @SuppressWarnings("unchecked")
                ListView<String> listView = (ListView<String>) event.getSource();
                listView.getItems().add(item);
                event.setDropCompleted(true);
                event.consume();
            }
        }
    };

    @Override
    protected Parent getRootNode() {
        list1.setOnDragDetected(onDragDetected);
        list2.setOnDragDetected(onDragDetected);
        list1.setOnDragDone(onDragDone);
        list2.setOnDragDone(onDragDone);
        list1.setOnDragOver(onDragOver);
        list2.setOnDragOver(onDragOver);
        list1.setOnDragEntered(onDragOver);
        list2.setOnDragEntered(onDragOver);
        list1.setOnDragDropped(onDragDropped);
        list2.setOnDragDropped(onDragDropped);
        
        return HBoxBuilder.create().children(list1, list2).build();
    }

    @SuppressWarnings("unchecked")
    @Test
	public void shouldMoveElements() throws Exception {
	    
        FXTestUtils.invokeAndWait(new Runnable() {
            public void run() {
                list1.getItems().addAll("A", "B", "C");
                list2.getItems().addAll("X", "Y", "Z");                
            }
        }, 1000);
	    
	    verifyThat(numberOfRowsIn(list1), is(3));
	    verifyThat(numberOfRowsIn(list2), is(3));
	    
	    drag("A").to("X");
	    verifyThat(numberOfRowsIn(list1), is(2));
        verifyThat(numberOfRowsIn(list2), is(4));
	    verifyThat(list2, containsRow("A"));
	    verifyThat(list1, not(containsRow("A")));
	    
	    drag("Z").to("B");
	    drag("C").to("B"); // Should have no effect
	    verifyThat(list1.getItems().size(), is(3));
        verifyThat(list2.getItems().size(), is(3));
        verifyThat(list1.getItems(), hasItems("Z", "B", "C"));
        verifyThat(list2.getItems(), hasItems("A", "X", "Y"));
	    
	}
}

package fileio;

import java.util.List;
import structures.SinglyLinkedList;

public interface IFileReadWrite<E, C> {    
    C read() throws Exception;
    boolean write(C container) throws Exception;       
}

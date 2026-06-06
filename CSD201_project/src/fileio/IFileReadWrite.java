package fileio;

import java.util.List;

public interface IFileReadWrite<E> {    
    List<E> read() throws Exception;
    boolean write(List<E> list) throws Exception;       
}

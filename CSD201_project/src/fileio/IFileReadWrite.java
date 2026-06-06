package fileio;

import structures.List;

public interface IFileReadWrite<E> {    
    List<E> read() throws Exception;
    boolean write(List<E> list) throws Exception;       
}

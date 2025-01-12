package INTERFACE;
import java.util.List;

public interface IDAO<T> {
    void save(T obj);
    void delete(int id);
    List<T> findAll();
    List<T> search(String mot);
    T findById(int id);
}

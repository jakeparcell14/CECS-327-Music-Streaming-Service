import java.io.Serializable;

public class Meow implements Serializable{
    int numofmeows;
    String meow;
    
    @Override
    public String toString() {
    	return meow + " " + numofmeows;
    }
}

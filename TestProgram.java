import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

//Class my entry
class MyEntry {
    private Integer key;
    private String value;
    public MyEntry(Integer key, String value) {
        this.key = key;
        this.value = value;
    }
    public Integer getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }
    @Override
    public String toString() {
        return key + " " + value;
    }
}

//Class SkipListPQ
class SkipListPQ {

    private class Node{
        private MyEntry entry;
        private Integer highest_level = 0;
        private Node left;
        private Node up;
        private Node right ;
        private Node down;

        public Node(Integer key, String value, Integer max_lvl){          
            entry =  new MyEntry(key, value); 
            highest_level = max_lvl;  
            left = null;
            up = null;
            right = null;
            down = null;
        }
        public Integer get_maxlevel(){return highest_level;}
        public MyEntry get_Entry(){return entry;}

        public Node get_leftNode(){return left;}; 
        public void set_leftNode(Node n){ left = n ;};

        public Node get_rightNode(){return right;}; 
        public void set_rightNode(Node n){ right = n ;};

        public Node get_upperNode(){return up;}; 
        public void set_upperNode(Node n){ up = n ;};

        public Node get_bottomNode(){return down;}; 
        public void set_bottomNode(Node n){ down = n ;};
    }

    private double alpha;
    private Random rand;
    private Node head;
    private Integer top_level;

    public SkipListPQ(double alpha) {
        this.alpha = alpha;
        this.rand = new Random();
        head = new Node(null, null, 0);
        head.set_rightNode(new Node(null, null, 0)); // creo le prime due sentinelle   
        top_level = 0; // ho solo il livello con i nodi sentinella
    }

    // Θ( n^2)
    public int size() {
    // dato che nel livello più in alto non c'è un nodo allora scendo subito al nodo successivo
	    int counter = 0;
        Node current = head.get_bottomNode();
        while(current != null && current.get_bottomNode() != null){
            Node tmp = current.get_rightNode();
            while(tmp != null && tmp.get_rightNode().get_Entry().getKey() != null){
                tmp =  tmp.get_rightNode();
                counter++;
            }
            current = current.get_bottomNode();
        }
        return counter;      
    }

    // Θ(n) deve scendere di tutti i livelli per cui
    // e appena arrivo al livello finale mi basta ritornare il la entry del nodo subito a destra
    public MyEntry min() {
	    Node currentNode = head;
        while(currentNode.get_bottomNode() != null){
            currentNode = currentNode.get_bottomNode();
        }
        return currentNode.get_rightNode().get_Entry();  
    }

    public int insert(int key, String value) {
        int level = generateEll(alpha, key);
        Node[] flags = new Node[level];
        Node current =  head;
        Integer to_save =  level - top_level;
        while(current != null){
            while(current.get_rightNode().get_Entry().getKey() != null && current.get_rightNode().get_Entry().getKey() < key ){
                current = current.get_rightNode();
            }
            if( to_save >= 0){
                flags[to_save] = current;
            }
            to_save++;
            current =  current.get_bottomNode();
        }
        
        //significa che devo aggiungere altri livelli siccome 
        //è stato estratto un livello troppo alto

        if(level >= top_level){
            Integer level_to_add = level - top_level;
            Node end_sentinel = head.get_rightNode();
            while(level_to_add >= 0){

                top_level++;
                head.set_upperNode(new Node(null, null, top_level));
                end_sentinel.set_upperNode(new Node(null, null, top_level));
                head.get_upperNode().set_bottomNode(head);
                end_sentinel.get_upperNode().set_bottomNode(end_sentinel);
                head = head.get_upperNode();
                end_sentinel = end_sentinel.get_upperNode();
                head.set_leftNode(end_sentinel);
                end_sentinel.set_rightNode(head);
                
                level_to_add--;
                if(level_to_add >= 0) flags[level_to_add] = head; 
            }
        }

        Node tmp = new Node(key , value, top_level);
        tmp.set_leftNode(flags[flags.length - 1]);
        tmp.set_rightNode(flags[flags.length - 1].get_rightNode());
        flags[flags.length - 1].get_rightNode().set_leftNode(tmp);
        flags[flags.length - 1].set_rightNode(tmp);
        for(int i = flags.length - 2; i >= 0; i--){
            tmp = new Node(key , value, top_level);
            tmp.set_leftNode(flags[i]);
            tmp.set_rightNode(flags[i].get_rightNode());
            flags[i].get_rightNode().set_leftNode(tmp);
            flags[i].set_rightNode(tmp);
            tmp.set_bottomNode(flags[i].get_bottomNode().get_rightNode());
        }

        return level;
    }

    private int generateEll(double alpha_ , int key) {
        int level = 0;
        if (alpha_ >= 0. && alpha_< 1) {
          while (rand.nextDouble() < alpha_) {
              level += 1;
          }
        }
        else{
          while (key != 0 && key % 2 == 0){
            key = key / 2;
            level += 1;
          }
        }
        return level;
    }


    public MyEntry removeMin() {
	// TO BE COMPLETED 
    // mi basta scendere completamente e poi rimuovo 
    // head.get_bottomNode().set_bottomNode(head.get_leftNode());
    Node currentNode = head;
    if(top_level == 0) return null; // perchè ho solo la linea con le sentinalle
    while(currentNode.get_bottomNode() != null){
        currentNode = currentNode.get_bottomNode(); 
    }
    currentNode.get_rightNode();
    return currentNode.get_Entry();
    }

    public void print() {
	Node currentNode = head;
    String text = "";
    while(currentNode.get_bottomNode() != null) currentNode = currentNode.get_bottomNode();
    while(currentNode.get_rightNode() != null){
        text += currentNode.get_Entry() + " " + currentNode.get_maxlevel() + 1 +",";
    }
    System.out.println(text);
    }
}

//TestProgram

public class TestProgram {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java TestProgram <file_path>");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            String[] firstLine = br.readLine().split(" ");
            int N = Integer.parseInt(firstLine[0]);
            double alpha = Double.parseDouble(firstLine[1]);
            System.out.println(N + " " + alpha);

            SkipListPQ skipList = new SkipListPQ(alpha);

            for (int i = 0; i < N; i++) {
                String[] line = br.readLine().split(" ");
                int operation = Integer.parseInt(line[0]);

                switch (operation) {
                    case 0:
			// TO BE COMPLETED 
                        break;
                    case 1:
			// TO BE COMPLETED 
                        break;
                    case 2:
			// TO BE COMPLETED 
                        break;
                    case 3:
			// TO BE COMPLETED 
                        break;
                    default:
                        System.out.println("Invalid operation code");
                        return;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
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
        head.get_rightNode().set_leftNode(head);
        top_level = 1;
        head.set_upperNode(new Node(null, null, top_level));
        head.get_rightNode().set_upperNode(new Node(null, null, top_level));
        head.get_upperNode().set_bottomNode(head);
        head.get_rightNode().get_upperNode().set_bottomNode(head.get_rightNode());
        head.get_upperNode().set_rightNode(head.get_rightNode().get_upperNode());
        head = head.get_upperNode();
        head.get_rightNode().set_leftNode(head);
    }

    public int size() {
	    int counter = 0;
        Node current = head;
        while(current.get_bottomNode() != null){
            current = current.get_bottomNode();
        }
        current = current.get_rightNode(); //lo sposto di uno a destra perchè nella prima colonna sono presenti le sentinelle
        while(current.get_Entry().getKey() != null){
            if(current != null)counter++;
            current = current.get_rightNode();
        }
        return counter;      
    }

    // deve scendere di tutti i livelli
    // e appena raggiunge s0 , ritorna la entry del nodo subito a destra
    public MyEntry min() {
	    Node currentNode = head;
        while(currentNode.get_bottomNode() != null){
            currentNode = currentNode.get_bottomNode();
        }
        return currentNode.get_rightNode().get_Entry();  
    }

    /*
     * Genera fino a che livello i nodi devono essere creati
     * poi cerca la posizione in cui dover inserire il nodo e nel mentre salva 
     * i nodi ai quali viene aggiunto alla loro destra il nuovo nodo 
     */
    public int insert(int key, String value) {
        int nodes_traversed = 0;
        int level = generateEll(alpha, key);
        Node[] flags = new Node[level + 1];
        Node current =  head;
        Integer to_save =  level - top_level;
        while(current != null){
            if(current != null) nodes_traversed++;
            while(current.get_rightNode().get_Entry().getKey() != null && current.get_rightNode().get_Entry().getKey() < key ){
                current = current.get_rightNode();
                nodes_traversed++;
            }
            if( to_save >= 0){
                flags[to_save] = current;
            }
            to_save++;
            current =  current.get_bottomNode();
        }
        
        //significa che devo aggiungere altri livelli siccome 
        //è stato estratto un livello più alto o uguale al numero di sequenze presenti ora
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
                head.set_rightNode(end_sentinel);
                end_sentinel.set_leftNode(head);
                
                level_to_add--;
                if(level_to_add >= 0) flags[level_to_add] = head; 
            }
        }

        Node tmp = new Node(key , value, level);
        tmp.set_leftNode(flags[flags.length - 1]);
        tmp.set_rightNode(flags[flags.length - 1].get_rightNode());
        flags[flags.length - 1].get_rightNode().set_leftNode(tmp);
        flags[flags.length - 1].set_rightNode(tmp);
        for(int i = flags.length - 2; i >= 0; i--){
            tmp = new Node(key , value, level);
            tmp.set_leftNode(flags[i]);
            tmp.set_rightNode(flags[i].get_rightNode());
            flags[i].get_rightNode().set_leftNode(tmp);
            flags[i].set_rightNode(tmp);
            tmp.set_bottomNode(flags[i].get_bottomNode().get_rightNode());
            tmp.get_bottomNode().set_upperNode(tmp);
        }

        return nodes_traversed;
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

    /*
     * Controlla se c'è solo il livello con 2 sentinelle e se ce ne sono di più
     * scende fino a s0 e prende il valore subito alla destra dell'ultima sentinella della prima colonna
     * poi risale fino all'ultimo livello in cui è definito il valore minimo e se era presente nel penultimo livello 
     * (quello prima del livello con solo le sentinelle) allora comincia ad eliminare sentinelle solo se non ci sono nodi tra le sentinelle
     */
    public MyEntry removeMin() {
        Node currentNode = head;
        if(top_level == 0) return null; // perchè ho solo la linea con le sentinalle
        while(currentNode.get_bottomNode() != null){
            currentNode = currentNode.get_bottomNode(); 
        }
        currentNode =  currentNode.get_rightNode();
        MyEntry entry = currentNode.get_Entry();
        Integer level_node = currentNode.get_maxlevel();
        //Node tmp1, tmp2 ;
        while(currentNode != null){
            //tmp1 = currentNode.get_rightNode();
            //tmp2 = currentNode.get_leftNode();
            
            currentNode.get_rightNode().set_leftNode(currentNode.get_leftNode());
            currentNode.get_leftNode().set_rightNode(currentNode.get_rightNode());
            //tmp2.set_rightNode(tmp1);
            //tmp1.set_leftNode(tmp2);
            currentNode.set_rightNode(new Node(null, null, level_node));
            currentNode.set_leftNode(new Node(null, null, level_node));
            currentNode = currentNode.get_upperNode();
            if(currentNode != null ) currentNode.get_bottomNode().set_upperNode(new Node(null, null, level_node));
        }
        if(level_node == (top_level - 1)){
            Node end_sentinel = head.get_rightNode();
            while((head.get_bottomNode() != null) && (head.get_bottomNode().get_rightNode() == end_sentinel.get_bottomNode())){
                head = head.get_bottomNode();
                end_sentinel = end_sentinel.get_bottomNode();
                head.get_upperNode().set_bottomNode(null);
                end_sentinel.get_upperNode().set_bottomNode(null);
                head.set_upperNode(null);
                end_sentinel.set_upperNode(null);
                top_level--;
            }
        }
        return entry;

    }

    /* 
     * si porta fino ad s0 e per ogni nodo presente , 
     * concatena l'entry in una stringa e poi stampa la stringa
    */
    public void print() {
        
        Node currentNode = head;
        String text = "";
    
        while(currentNode.get_bottomNode() != null) currentNode = currentNode.get_bottomNode();
    
        currentNode = currentNode.get_rightNode();

        while(currentNode.get_rightNode().get_Entry().getKey() != null){ //continuo a fare il ciclo fino  a quando non trovo il nodo prima della sentinella che mi indica la fine del livello
            text += currentNode.get_Entry() + " " + (currentNode.get_maxlevel() + 1) +",";
            currentNode = currentNode.get_rightNode();
        }

        text += currentNode.get_Entry() + " " + (currentNode.get_maxlevel() + 1);

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
            long nodes_traversed = 0;
            int num_of_inserts = 0;
            for (int i = 0; i < N; i++) {
                String[] line = br.readLine().split(" ");
                int operation = Integer.parseInt(line[0]);

                switch (operation) {
                    case 0:
                        System.out.println(skipList.min());
                        break;
                    case 1:
                        skipList.removeMin(); 
                        break;
                    case 2:
                        nodes_traversed += skipList.insert(Integer.parseInt(line[1]), line[2]);
                        num_of_inserts++;
                        break;
                    case 3:
                        skipList.print();
                        break;
                    default:
                        System.out.println("Invalid operation code");
                        return;
                }
            }
            System.out.println(alpha + " " + skipList.size() + " " + num_of_inserts + " " + ((double)nodes_traversed /(double)num_of_inserts));
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
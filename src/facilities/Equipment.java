package facilities;


import enums.OperationalStatus;

public class Equipment {

    private String name;
    private OperationalStatus status;

    public Equipment(String name,OperationalStatus status){
        
        if(name == null || name.isBlank()){
                throw new IllegalArgumentException("Invalid Equipment Name");
            }
        if (status == null){
                throw new IllegalArgumentException("Invalid Equipment Status");
            }
        this.name = name;
        this.status = status;
        }
        public String getName(){return this.name;}
        public  OperationalStatus getStatus(){return this.status;}

        public void setName(String name){
            if (name == null || name.isBlank()){
                throw new IllegalArgumentException("Invalid Equipment Name");
            }
        }
    
    
}

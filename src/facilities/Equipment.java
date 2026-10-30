package facilities;


import enums.OperationalStatus;

public class Equipment {

    private String name;    
    private OperationalStatus operationalStatus;

    public Equipment(String name,OperationalStatus operationalStatus){
        
        if(name == null || name.isBlank()){
                throw new IllegalArgumentException("Invalid Equipment Name");
            }
        if (operationalStatus == null){
                throw new IllegalArgumentException("Invalid Equipment Status");
            }
        this.name = name;
        this.operationalStatus = operationalStatus;
        }
        public String getName(){return this.name;}
            public OperationalStatus getOperationalStatus(){return this.operationalStatus;}

        public void setName(String name){
            if (name == null || name.isBlank()){
                throw new IllegalArgumentException("Invalid Equipment Name");
            }
            this.name = name;
        }
        public void  setOperationalStatus(OperationalStatus operationalStatus)
        {
        if (operationalStatus == null)
            throw new IllegalArgumentException("Invalid Operational Status");
        else this.operationalStatus = operationalStatus;
        }
    
}

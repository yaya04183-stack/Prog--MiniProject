package facilities;


import enums.OperationalStatus;

public class Equipment {

    public enum Category {
        GENERAL,
        SCIENTIFIC
    }

    private String name;    
    private OperationalStatus operationalStatus;
    private Category category;

    public Equipment(String name,OperationalStatus operationalStatus){
        this(name, operationalStatus, Category.GENERAL);
    }

    public Equipment(String name, OperationalStatus operationalStatus, Category category) {
        
        if(name == null || name.isBlank()){
                throw new IllegalArgumentException("Invalid Equipment Name");
            }
        if (operationalStatus == null){
                throw new IllegalArgumentException("Invalid Equipment Status");
            }
        if (category == null) {
                throw new IllegalArgumentException("Invalid Equipment Category");
            }
        this.name = name;
        this.operationalStatus = operationalStatus;
        this.category = category;
        }
        public String getName(){return this.name;}
            public OperationalStatus getOperationalStatus(){return this.operationalStatus;}
        public Category getCategory(){return this.category;}

        public void setName(String name){
            if (name == null || name.isBlank()){
                throw new IllegalArgumentException("Invalid Equipment Name");
            }
            this.name = name;
        }
        public void setCategory(Category category) {
            if (category == null) {
                throw new IllegalArgumentException("Invalid Equipment Category");
            }
            this.category = category;
        }
        public void  setOperationalStatus(OperationalStatus operationalStatus)
        {
        if (operationalStatus == null)
            throw new IllegalArgumentException("Invalid Operational Status");
        else this.operationalStatus = operationalStatus;
        }
    
}

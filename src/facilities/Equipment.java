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

    // Initializes a Equipment instance.
    // Initializes a Equipment instance.
    public Equipment(String name,OperationalStatus operationalStatus){
        this(name, operationalStatus, Category.GENERAL);
    }

    // Initializes a Equipment instance.
    // Initializes a Equipment instance.
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
        // Getters

        // Returns name.
        public String getName(){return this.name;}
            // Returns operational status.
            public OperationalStatus getOperationalStatus(){return this.operationalStatus;}
        // Returns category.
        public Category getCategory(){return this.category;}

        // Setters

        // Sets name.
        public void setName(String name){
            if (name == null || name.isBlank()){
                throw new IllegalArgumentException("Invalid Equipment Name");
            }
            this.name = name;
        }
        // Sets category.
        public void setCategory(Category category) {
            if (category == null) {
                throw new IllegalArgumentException("Invalid Equipment Category");
            }
            this.category = category;
        }
        // Sets operational status.
        public void  setOperationalStatus(OperationalStatus operationalStatus)
        {
        if (operationalStatus == null)
            throw new IllegalArgumentException("Invalid Operational Status");
        else this.operationalStatus = operationalStatus;
        }
    
}

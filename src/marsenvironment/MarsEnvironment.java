package marsenvironment;

import enums.EnviromentStates;

public class MarsEnvironment {

    private EnviromentStates environmentState;

    public MarsEnvironment(EnviromentStates environmentState){
        SetEnvironmentState(environmentState);
    }

    public EnviromentStates GetEnvironmentState() {
        return environmentState;
    }

    public void SetEnvironmentState(EnviromentStates environmentState) {
        if (environmentState == null) {
            throw new IllegalArgumentException("Environment state cannot be null");
        }
        this.environmentState = environmentState;
    }

    public int GetEnvironmentStateCode() {
        switch (environmentState) {
            case DUST_STORM:
                return 1;
            case COMMUNICATION_DISTRIBUTION:
                return 2;
            case NORMAL_CONDITION:
                return 3;
            default:
                throw new IllegalArgumentException("Unknown environment state: " + environmentState);
        }
    }


}

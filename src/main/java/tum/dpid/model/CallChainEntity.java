package tum.dpid.model;

import java.util.Objects;

public class CallChainEntity {

    private String name;
    private String invokedMethod;
    private boolean invokesChildInLoop;

    public CallChainEntity(String name, String invokedMethod, boolean invokesChildInLoop) {
        this.name = name;
        this.invokedMethod = invokedMethod;
        this.invokesChildInLoop = invokesChildInLoop;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInvokedMethod() {
        return invokedMethod;
    }

    public void setInvokedMethod(String invokedMethod) {
        this.invokedMethod = invokedMethod;
    }

    public boolean isInvokesChildInLoop() {
        return invokesChildInLoop;
    }

    public void setInvokesChildInLoop(boolean invokesChildInLoop) {
        this.invokesChildInLoop = invokesChildInLoop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallChainEntity entity = (CallChainEntity) o;
        return Objects.equals(name, entity.name) && Objects.equals(invokedMethod, entity.invokedMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, invokedMethod);
    }
}

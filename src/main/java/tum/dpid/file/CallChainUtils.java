package tum.dpid.file;

import tum.dpid.model.CallChainEntity;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CallChainUtils {

    public static void removeSublists(List<List<CallChainEntity>> allChains) {
        Iterator<List<CallChainEntity>> iterator = allChains.iterator();

        while (iterator.hasNext()) {
            List<CallChainEntity> currentList = iterator.next();
            for (List<CallChainEntity> otherList : allChains) {
                if (otherList == currentList) continue; // Skip comparing the same list
                if (Collections.indexOfSubList(otherList, currentList) != -1) {
                    iterator.remove(); // Remove currentList if it is a sublist of otherList
                    break; // Break out of the loop to avoid modifying the list while iterating
                }
            }
        }
    }
}

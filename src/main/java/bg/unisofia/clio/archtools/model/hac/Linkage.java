package bg.unisofia.clio.archtools.model.hac;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.CompleteLinkageStrategy;
import com.apporiented.algorithm.clustering.LinkageStrategy;
import com.apporiented.algorithm.clustering.SingleLinkageStrategy;

public enum Linkage {

    SINGLE(new SingleLinkageStrategy()),
    AVERAGE(new AverageLinkageStrategy()),
    COMPLETE(new CompleteLinkageStrategy());

    public LinkageStrategy strategy;

    private Linkage(LinkageStrategy strategy) {
        this.strategy = strategy;
    }
}

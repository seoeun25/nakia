package com.lezhin.avengers.panther.command;

import com.lezhin.avengers.panther.exception.PreconditionException;
import com.lezhin.avengers.panther.model.Payment;
import com.lezhin.avengers.panther.model.RequestInfo;

/**
 * @author seoeun
 * @since 2017.10.24
 */
public class Complete<P extends Payment> extends Command<P> {
    public Complete(RequestInfo requestInfo) {
        super(requestInfo);
    }

    public void verifyPrecondition() throws PreconditionException {
        //TODO
        // PV. pay완료 (단, C-코인충전 미완료 recovery data 혹은 영수증 데이터를 이용하여 최종적으로 검증하고 코인충전 과정이 필요함.)
        // 이걸 complete 에서 해야 할 듯
    }

    @Override
    public P execute() {
        return null;
    }
}

package edu.iis.mto.testreactor.exc3;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.Mock;

public class AtmMachineTest {

    @Mock
    CardProviderService cardProviderService;

    @Mock
    BankService bankService;

    @Mock
    MoneyDepot moneyDepot;

    @Test
    public void itCompiles() {
        assertThat(true, equalTo(true));
    }

}

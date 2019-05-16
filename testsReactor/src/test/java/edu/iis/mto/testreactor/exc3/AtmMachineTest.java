package edu.iis.mto.testreactor.exc3;

import static edu.iis.mto.testreactor.exc3.Banknote.*;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.*;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.reflect.Parameter;

public class AtmMachineTest {

    CardProviderService cardProviderService = mock(CardProviderService.class);
    BankService bankService = mock(BankService.class);
    MoneyDepot moneyDepot = mock(MoneyDepot.class);

    AtmMachine sut;

    @Before
    public void setUp() throws Exception {
        sut = new AtmMachine(cardProviderService, bankService, moneyDepot);
    }

    @Test
    public void itCompiles() {
        assertThat(true, equalTo(true));
    }

    @Test
    public void paymentShouldContainDesiredAmountOfMoney() throws CardAuthorizationException {
        Money money = Money.builder().withAmount(100).withCurrency(Currency.PL).build();
        Card card = Card.builder().withCardNumber("1234").withPinNumber(1234).build();

        when(cardProviderService.authorize(any())).thenReturn(AuthenticationToken.builder().withAuthorizationCode(1234).withUserId("1234").build());

        Payment result = sut.withdraw(money, card);

        assertThat(result.getValue().size(), is(1));
        assertThat(result.getValue().get(0), is(PL100));
    }

    @Test(expected = WrongMoneyAmountException.class)
    public void shouldNotWithrawImpossibleAmountOfMoney() {
        Money money = Money.builder().withAmount(44).withCurrency(Currency.PL).build();
        Card card = Card.builder().withCardNumber("1234").withPinNumber(1234).build();

        // Quick workaround for Mockito complaining about possible exception
       try {
           when(cardProviderService.authorize(any())).thenReturn(AuthenticationToken.builder().withAuthorizationCode(1234).withUserId("1234").build());
       } catch (Exception e) {
           System.err.println(e);
           fail();
       }

        sut.withdraw(money, card);
    }

    @Test
    public void verificationShouldBePerformed() throws CardAuthorizationException {
        Money money = Money.builder().withAmount(100).withCurrency(Currency.PL).build();
        Card card = Card.builder().withCardNumber("1234").withPinNumber(1234).build();

        // Quick workaround for Mockito complaining about possible exception
        try {
            when(cardProviderService.authorize(any())).thenReturn(AuthenticationToken.builder().withAuthorizationCode(1234).withUserId("1234").build());
        } catch (Exception e) {
            System.err.println(e);
            fail();
        }

        sut.withdraw(money, card);

        verify(cardProviderService, times(1)).authorize(card);
    }

    @Test
    public void withdrawnMoneyShouldBeSubtractedFromAccount() throws InsufficientFundsException {
        Money money = Money.builder().withAmount(100).withCurrency(Currency.PL).build();
        Card card = Card.builder().withCardNumber("1234").withPinNumber(1234).build();

        // Quick workaround for Mockito complaining about possible exception
        try {
            when(cardProviderService.authorize(any())).thenReturn(AuthenticationToken.builder().withAuthorizationCode(1234).withUserId("1234").build());
        } catch (Exception e) {
            System.err.println(e);
            fail();
        }

        sut.withdraw(money, card);

        verify(bankService, times(1)).charge(any(), eq(money));
    }

    @Test
    public void transactionShouldBeAbortedInCaseOfLackingFunds() {
        Money money = Money.builder().withAmount(100).withCurrency(Currency.PL).build();
        Card card = Card.builder().withCardNumber("1234").withPinNumber(1234).build();

        // Quick workaround for Mockito complaining about possible exception
        try {
            when(cardProviderService.authorize(any())).thenReturn(AuthenticationToken.builder().withAuthorizationCode(1234).withUserId("1234").build());
            doThrow(InsufficientFundsException.class).when(bankService).charge(any(), any());
            sut.withdraw(money, card);

            fail();
        } catch (Exception e) {
            verify(bankService, times(1)).abort(any());
        }
    }
}

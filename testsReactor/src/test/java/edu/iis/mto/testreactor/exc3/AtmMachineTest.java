package edu.iis.mto.testreactor.exc3;

import org.junit.Before;
import org.junit.Test;

import static edu.iis.mto.testreactor.exc3.Banknote.*;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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

    @Test
    public void theSameAuthenticationTokenShouldBePassedToBank() {
        Money money = Money.builder().withAmount(100).withCurrency(Currency.PL).build();
        Card card = Card.builder().withCardNumber("1234").withPinNumber(1234).build();

        AuthenticationToken authenticationToken = AuthenticationToken.builder().withAuthorizationCode(1234).withUserId("1234").build();

        // Quick workaround for Mockito complaining about possible exception
        try {
            when(cardProviderService.authorize(any())).thenReturn(AuthenticationToken.builder().withAuthorizationCode(1234).withUserId("1234").build());
        } catch (Exception e) {
            System.err.println(e);
            fail();
        }

        sut.withdraw(money, card);

        verify(bankService, times(1)).startTransaction(eq(authenticationToken));
    }

    @Test
    public void moneyShouldBeRemovedFromTheDepot() throws MoneyDepotException {
        Money money = Money.builder().withAmount(100).withCurrency(Currency.PL).build();
        Card card = Card.builder().withCardNumber("1234").withPinNumber(1234).build();

        // Quick workaround for Mockito complaining about possible exception
        try {
            when(cardProviderService.authorize(any())).thenReturn(AuthenticationToken.builder().withAuthorizationCode(1234).withUserId("1234").build());
        } catch (Exception e) {
        }

        sut.withdraw(money, card);

        verify(moneyDepot, times(1)).releaseBanknotes(any());
    }

    @Test
    public void atmShouldReturnPaymentInTheBiggestPossibleBanknotes() throws CardAuthorizationException {
        Money money = Money.builder().withAmount(280).withCurrency(Currency.PL).build();
        Card card = Card.builder().withCardNumber("1234").withPinNumber(1234).build();

        when(cardProviderService.authorize(any())).thenReturn(AuthenticationToken.builder().withAuthorizationCode(1234).withUserId("1234").build());

        Payment result = sut.withdraw(money, card);

        // Should be, 200, 50, 20, 10 not 100, 100, 50, 20, 10 or anything like that
        assertThat(result.getValue().size(), is(4));
        assertThat(result.getValue().get(0), is(PL10));
        assertThat(result.getValue().get(1), is(PL20));
        assertThat(result.getValue().get(2), is(PL50));
        assertThat(result.getValue().get(3), is(PL200));
    }
}

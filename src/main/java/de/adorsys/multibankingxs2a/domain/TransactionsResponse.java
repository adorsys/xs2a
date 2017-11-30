package de.adorsys.multibankingxs2a.domain;


import java.util.List;

import lombok.Data;

/**
 * Created by aro on 27.11.17.
 */
@Data

public class TransactionsResponse  {

   private Links _links;
   private AccountReport transactions;

}



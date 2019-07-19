SELECT ev.*, 
       CASE 
         WHEN ev.consent_id IS NOT NULL THEN cst_psu_data.psu_id 
         ELSE pmt_psu_data.psu_id 
       END AS psu_ex_id, 
       CASE 
         WHEN ev.consent_id IS NOT NULL THEN cst_psu_data.psu_id_type 
         ELSE pmt_psu_data.psu_id_type 
       END AS psu_ex_id_type, 
       CASE 
         WHEN ev.consent_id IS NOT NULL THEN cst_psu_data.psu_corporate_id 
         ELSE pmt_psu_data.psu_corporate_id 
       END AS psu_ex_corporate_id, 
       CASE 
         WHEN ev.consent_id IS NOT NULL THEN cst_psu_data.psu_corporate_id_type 
         ELSE pmt_psu_data.psu_corporate_id_type 
       END AS psu_ex_corporate_id_type 
FROM   cms.event ev 
       LEFT JOIN cms.pis_common_payment pmt 
              ON ev.payment_id = pmt.payment_id 
       LEFT JOIN cms.ais_consent cst 
              ON ev.consent_id = cst.external_id 
       LEFT JOIN cms.pis_common_payment_psu_data pmt_psu 
              ON pmt.id = pmt_psu.pis_common_payment_id 
       LEFT JOIN cms.ais_consent_psu_data cst_psu 
              ON cst.id = cst_psu.ais_consent_id 
       LEFT JOIN cms.psu_data pmt_psu_data 
              ON pmt_psu_data.id = pmt_psu.psu_data_id 
       LEFT JOIN cms.psu_data cst_psu_data 
              ON cst_psu_data.id = cst_psu.psu_data_id 

//To generate screenshot:
//Create a new script.
//Copy the two queries below into studio, run them against a KDB server that has had data.q run on it and open the charts for both.
//Then query quoteOrTradeOrWhatever (which shows the table view at the bottom) and delete this query so it no longer shows up in the editor. But keep all the empty lines!
//Finally create a dummy server named "nyse". It doesn't have to be a real server, just make sure the hostname is not obscene.


/ Calculate the volume profile for GOOG based on 30 days trade data
select avg size by minute from
select sum size by date,10 xbar time.minute from trade
  where date within (-30+last date;last date),sym=`GOOG

/ Calculate the cumulative volume profile for GOOG based on 30 days trade data
update sums size from
select avg size by minute from
select sum size by date,10 xbar time.minute from trade
  where date within (-30+last date;last date),sym=`GOOG




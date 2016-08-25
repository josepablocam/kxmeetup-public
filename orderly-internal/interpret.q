/
  We create a rather simple interpreter for Orderly.
  Indeed, all this does is insert the AST into a table with appropriately
  named columns
\

toTable:{flip `side`volume`units`ticker`px`cond`client!(),/:x}
add:{[ast] (last ast) upsert toTable 7#(-1 _ ast),`self}
interpret:add

satisfies:{[t;env] select from t where first each @[;env;0b] each cond }



/
q)o) buy 100 shares of AAPL at $25.65 on 10/12/2009 -> t
q)o) buy 100 shares of AAPL at $25.65 if "{10<exec avg close_price from x}" -> t

mkt:([]close_price:100?100)
cal:enlist[`date]!enlist 2009.12.10

satisfies[t; mkt]
satisfies[t; cal]

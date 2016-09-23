/
  Orderly analyzer
  Provides some simple semantic checks/transformations on parsed results
\

// just for nice "assert-like" language
be:(::);
should:{[x;y] if[not y[0] x;'y[1][]]};
// capital sin! but we'll remove before we exit the script
.q.should:should;
// based on "AST" from parse
getVolume:@[;1];
getPrice:@[;4];
getModifier:@[;5];
setModifier:{@[x;5;:;y]};
// take a date and wrap in function
wrapDate:{$[type[.z.D]=type x;{[env;d] env[`date]=d}[;x];x]}
isFun:{@[{(0h=type value x)&100<=type x};x;0b]}
getArgs:{value[x] 1}

// is unary function (note we need to handle partial eval)
isUnary:{
  // resolve potential identifier
  f:$[type[`]=type x;get x;x];
  $[isFun f;
    1=$[isFun first fv:value f;
      // handle partial eval
      (count getArgs first fargs)-neg[1]+count fargs:{x where not x~\:(::)} fv;
      // normal
      count getArgs f
      ];
    0b]
  }
// main semantic check function
check0:{
  getVolume[x] should be ({x > 0};{"Volume should be positive"});
  getPrice[x] should be ({x > 0};{"Price should be positive"});
  x:setModifier[x;] wrapDate getModifier x;
  getModifier[x] should be (isUnary;{"Expected unary function"});
  x
  };

 check:{@[check0;x;{'x}]}

// we've atoned for our sins
.q: `should _ .q;

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

satisfies:{[t;env] select from t where first each @[;env;0b] each cond }

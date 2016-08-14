/
  Orderly parser
  Simple tokenization based on whitespace (so must be sure to write accordingly)
  Text within quotes is not split on whitespace to allow for arbitrary q code
\

// simple markers
ws:" \t\n"
quotes:"\""
// matching quotes
matchingQuotes:{0=(sum x in quotes) mod 2}
// split stream of text
tokens:{trim each (distinct 0, where (x in ws)&not (til count x) within (first;last)@\:where x in quotes) cut x }
// remove any tokens that are empty or just new lines
clean:{x where (any each not x in ws)&0<count each x}
// error message
error:{[msg;ctx] '"error: ",msg,$[ctx~();"";ctx]}
// tokenize if matching quotes okay
tokenize:{$[not matchingQuotes x;error["unclosed quotes";" at char ",string last where x in quotes];clean tokens x]}

// key words
kw:{any y~/:(x; upper x)}
BUY:kw "buy";
SELL:kw "sell";
OF:kw "of";
AT:kw "at";
SHARES:kw "shares";
IF:kw "if";
ON:kw "on";
FOR:kw "for";

// checks (apply first at end of each to handle empty lists `boolean$() -> 0b)
isKw:{first any (BUY;SELL;OF;AT;SHARES;IF;ON;FOR)@\:x}
isIdent:{first (not isKw x)&first[x] in .Q.a,.Q.A}
isNum:{first first[x] in "-.",.Q.n}
// we're a bit hacky here and accept any date format that q can parse
isDate:{@[{not null "D"$x};x;0b]}
// get string for certain error messages
getStr:{[s;i]$[0=count s i;"EMPTY";s i]}

// parsers
// take entire list of tokens and read off as many as needed
// if succeeds returns token value and number of tokens to remove from list
num:{$[isNum x 0;("F"$x 0;1);error["Expected num";" found ", getStr[x;0]]]}
ident:{$[isIdent x 0;(`$x 0;1);error["Expected identifier";" found ",getStr[x;0]]]}
// accept generic pattern (no returned token)
accept:{[p;s;e]$[p e 0;(();1);error["Expected ",s;" found ",getStr[e;0]]]}
side:{$[BUY x 0; (`buy;1); SELL x 0;(`sell;1); error["Expected side";" found ",getStr[x;0]]]}
vol:{
  $[SHARES x 1;
    (first num x;2);
  "$"~first x 0;
    (first num enlist 1_x 0;1);
  error["Expected volume";" found "," " sv (),/:2#x]
  ]
 }
modifier:{
  $[(IF x 0);
    $[first[x 1] in quotes;
        (value value x 1;2);
      isIdent x 1;
        (first ident 1_x; 2);
      error["Expected q lambda in quotes or function name"; " found ", getStr[x;1]]
    ];
    ON x 0;
      $[isDate x 1; ("D"$x 1;2); error["Expected date"; " found ", getStr[x;1]]];
    error["Expected modifier"; " found "," " sv (),/:2#x]
    ]
 }
forClause:{ $[FOR x 0;(first ident 1_x;2);(();0)] }

// apply a single parser to a stream of tokens, return token and modified stream
consume0:{[p;t] (first res;(last res:p t)_t)}
// consume a list of parsers
consume:{[ps;t] first each {consume0[y;last x]}\[(();t);ps]}

// orderly grammar as a list (we have a simple grammar :) )
grammar:(side;vol;accept[OF;"of"];ident;accept[AT;"at"];num;modifier;forClause;accept[{x~"->"};"->"];ident);

// wrap to avoid having errors return deeper functions (no need to worry
// user with implementation)
parser:{@[raze consume[grammar; ] tokenize@;x;{'x}]}


/
o) buy 100 shares of aapl at 100 on 2009-01-01 -> t
o) sell $1000 of BAML at 50.40 if "{100 < avg x`close_price}" -> t1

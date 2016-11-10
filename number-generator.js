'use strict';

var amountOfNumbers = 20;

var n = [];
for (var i = 0; i < amountOfNumbers; i++) {
    n[i] = i + 1;
}

shuffle(n);
console.log(n.toString());

function shuffle(a) {
    var j, x, i;
    for (i = a.length; i; i--) {
        j = Math.floor(Math.random() * i);
        x = a[i - 1];
        a[i - 1] = a[j];
        a[j] = x;
    }
}

let seq = core.load('hegemon/sequence');

function testForEachWithJSObject() {
  let results = [];
  let iterable = [1, 2, 3];
  seq.forEach(iterable, function(val) {
    results.push(val);
  });
  Assert.assertEquals(results, iterable);
}

function testForEachWithJavaObject() {
  let results = [];
  let list = java.util.LinkedList();
  list.addAll(['a', 'b', 'c']);
  seq.forEach(list, function(val) {
    results.push(val);
  });
  Assert.assertEquals(['a', 'b', 'c'], results);
}

function testForEachWithJavaArray() {
  let array = com.cueup.hegemon.stdlib.JavaJsTest.arrayOf('a', 'b', 'c');
  let results = [];
  seq.forEach(array, function(val) {
    results.push(val);
  });
  Assert.assertEquals(['a', 'b', 'c'], results);
}

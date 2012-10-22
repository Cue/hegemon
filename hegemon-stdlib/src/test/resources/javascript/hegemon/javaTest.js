load('hegemon/java');

function testForEachWithJSObject() {
  let results = [];
  let iterable = [1, 2, 3];
  forEach(iterable, function(val) {
    results.push(val);
  });
  Assert.assertEquals(results, iterable);
}

function testForEachWithJavaObject() {
  let results = [];
  let list = java.util.LinkedList();
  list.addAll(['a', 'b', 'c']);
  forEach(list, function(val) {
    results.push(val);
  });
  Assert.assertEquals(['a', 'b', 'c'], results);
}

function testForEachWithJavaArray() {
  let array = com.cueup.hegemon.stdlib.JavaJsTest.arrayOf('a', 'b', 'c');
  let results = [];
  forEach(array, function(val) {
    results.push(val);
  });
  Assert.assertEquals(['a', 'b', 'c'], results);
}

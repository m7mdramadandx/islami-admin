
import 'dart:convert';
import 'package:firebase_storage/firebase_storage.dart';
import 'package:http/http.dart' as http;

abstract class AzkarRemoteDataSource {
  Future<String> getAzkar();
  Future<void> saveAzkar(String content);
}

class AzkarRemoteDataSourceImpl implements AzkarRemoteDataSource {
  final FirebaseStorage _storage;

  AzkarRemoteDataSourceImpl(this._storage);

  @override
  Future<String> getAzkar() async {
    final ref = _storage.refFromURL('gs://islami-ecc03.appspot.com/json/azkar.json');
    final url = await ref.getDownloadURL();
    final response = await http.get(Uri.parse(url));
    if (response.statusCode == 200) {
      return utf8.decode(response.bodyBytes);
    } else {
      throw Exception('Failed to fetch azkar.json');
    }
  }

  @override
  Future<void> saveAzkar(String content) async {
    final ref = _storage.refFromURL('gs://islami-ecc03.appspot.com/json/azkar.json');
    final data = utf8.encode(content);
    await ref.putData(data, SettableMetadata(contentType: 'application/json'));
  }
}

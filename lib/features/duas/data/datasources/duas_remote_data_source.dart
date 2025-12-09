
import 'dart:convert';
import 'package:firebase_storage/firebase_storage.dart';

abstract class DuasRemoteDataSource {
  Future<String> getDuas();
  Future<void> saveDuas(String content);
}

class DuasRemoteDataSourceImpl implements DuasRemoteDataSource {
  final FirebaseStorage _storage;

  DuasRemoteDataSourceImpl(this._storage);

  @override
  Future<String> getDuas() async {
    final ref = _storage.refFromURL('gs://islami-ecc03.appspot.com/json/duas.json');
    final data = await ref.getData();
    if (data != null) {
      return utf8.decode(data);
    } else {
      throw Exception('Failed to load duas');
    }
  }

  @override
  Future<void> saveDuas(String content) async {
    final ref = _storage.refFromURL('gs://islami-ecc03.appspot.com/json/duas.json');
    final data = utf8.encode(content);
    await ref.putData(data, SettableMetadata(contentType: 'application/json'));
  }
}


abstract class DuasRepository {
  Future<String> getDuas();
  Future<void> saveDuas(String content);
}

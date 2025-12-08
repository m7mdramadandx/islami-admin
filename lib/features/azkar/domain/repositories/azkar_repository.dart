
abstract class AzkarRepository {
  Future<String> getAzkar();
  Future<void> saveAzkar(String content);
}

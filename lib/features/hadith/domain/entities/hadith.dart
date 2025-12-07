
class Hadith {
  final String id;
  final String text;
  final String narrator;

  Hadith({required this.id, required this.text, required this.narrator});

  factory Hadith.fromJson(Map<String, dynamic> json) {
    return Hadith(
      id: json['id'] as String,
      text: json['text'] as String,
      narrator: json['narrator'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'text': text,
      'narrator': narrator,
    };
  }
}

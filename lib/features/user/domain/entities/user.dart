
class User {
  final String id;
  final String name;
  final String email;
  final bool isAdmin;

  User({required this.id, required this.name, required this.email, this.isAdmin = false});

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] as String,
      name: json['name'] as String,
      email: json['email'] as String,
      isAdmin: json['isAdmin'] as bool,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'isAdmin': isAdmin,
    };
  }
}

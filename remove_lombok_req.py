import os
import re

def remove_required_args(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    if '@RequiredArgsConstructor' in content:
        # Remove import
        content = content.replace('import lombok.RequiredArgsConstructor;\n', '')
        # Remove annotation
        content = content.replace('@RequiredArgsConstructor\n', '')
        
        # Find class name
        class_match = re.search(r'public class (\w+)', content)
        if not class_match:
            return
            
        class_name = class_match.group(1)
        
        # Find private final fields
        field_matches = re.findall(r'private final ([\w<>]+) (\w+);', content)
        
        if field_matches:
            # Generate constructor
            constructor = f'\n    public {class_name}('
            constructor += ', '.join([f'{type} {name}' for type, name in field_matches])
            constructor += ') {\n'
            for type, name in field_matches:
                constructor += f'        this.{name} = {name};\n'
            constructor += '    }\n'
            
            # Find the last private final field to insert constructor after it
            last_field = f'private final {field_matches[-1][0]} {field_matches[-1][1]};'
            content = content.replace(last_field, last_field + '\n' + constructor)

        with open(file_path, 'w') as f:
            f.write(content)
        print(f'Processed @RequiredArgsConstructor in {file_path}')

files = [
    'src/main/java/com/virtualfit/ai/config/SecurityConfig.java',
    'src/main/java/com/virtualfit/ai/security/JwtAuthenticationFilter.java',
    'src/main/java/com/virtualfit/ai/security/CustomUserDetailsService.java',
    'src/main/java/com/virtualfit/ai/controller/ProfileController.java',
    'src/main/java/com/virtualfit/ai/controller/ShopController.java',
    'src/main/java/com/virtualfit/ai/controller/StyleFitController.java',
    'src/main/java/com/virtualfit/ai/controller/AuthController.java',
    'src/main/java/com/virtualfit/ai/service/HuggingFaceService.java',
    'src/main/java/com/virtualfit/ai/service/ProductService.java',
    'src/main/java/com/virtualfit/ai/service/StyleFitService.java',
    'src/main/java/com/virtualfit/ai/service/AuthService.java'
]

for file in files:
    remove_required_args(file)

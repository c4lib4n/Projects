pub fn convert_to_int(data_input: &String) -> i32{
    let x: i32 = data_input.trim().parse::<i32>().unwrap();
    x
}
